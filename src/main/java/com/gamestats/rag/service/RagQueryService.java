package com.gamestats.rag.service;

import com.gamestats.rag.dto.QueryResponse;
import com.gamestats.rag.model.*;
import com.gamestats.rag.repository.ConsultaIARepository;
import com.gamestats.rag.repository.DocumentChunkRepository;
import com.gamestats.rag.repository.RespuestaIARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * EL CORAZON DEL RAG. Responde preguntas en 4 pasos y guarda todo en el historial:
 *
 *  0. HISTORIAL   - Registra ConsultaIA en estado PENDIENTE
 *  1. RETRIEVAL   - Convierte la pregunta en embedding
 *  2. RETRIEVAL   - Busca en pgvector los K fragmentos mas parecidos (solo del usuario)
 *  3. AUGMENTED   - Arma un prompt con esos fragmentos numerados como contexto
 *  4. GENERATION  - El LLM responde usando SOLO ese contexto y citando [1], [2]...
 *  5. PERSISTENCIA- Guarda RespuestaIA + FuenteRespuesta y actualiza el estado
 *                   (RESPONDIDA, SIN_RESULTADOS o ERROR si algo falla)
 */
@Service
@RequiredArgsConstructor
public class RagQueryService {

    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository chunkRepository;
    private final OllamaLlmService llmService;
    private final ConsultaIARepository consultaRepository;
    private final RespuestaIARepository respuestaRepository;

    @Value("${app.rag.top-k}")
    private int topK;

    // Similitud minima para usar un fragmento como fuente (ver application.yml)
    @Value("${app.rag.min-similarity:0.40}")
    private double minSimilarity;

    public QueryResponse ask(String question, User user) {
        long start = System.currentTimeMillis();

        // Guardar la consulta desde el inicio en estado PENDIENTE.
        // No se usa @Transactional en todo el metodo para que, si falla Ollama,
        // el registro con estado ERROR quede guardado en base de datos.
        ConsultaIA consulta = consultaRepository.save(ConsultaIA.builder()
                .user(user)
                .pregunta(question)
                .estado(EstadoConsulta.PENDIENTE)
                .build());

        try {
            // Pasos 1 y 2: embedding de la pregunta + busqueda semantica con similitud
            String queryEmbedding = embeddingService.embed(question);
            List<Object[]> candidatos = chunkRepository.findSimilarChunkIdsWithSimilarity(
                    user.getId(), queryEmbedding, topK);

            if (candidatos.isEmpty()) {
                return responderSinResultados(consulta, question,
                        "No tienes documentos indexados todavia. Sube un PDF de un videojuego para empezar.", start);
            }

            // UMBRAL: descartar fragmentos poco parecidos. Si ninguno lo supera,
            // NO se llama al LLM: asi es imposible que invente una respuesta.
            List<Object[]> rows = candidatos.stream()
                    .filter(r -> ((Number) r[1]).doubleValue() >= minSimilarity)
                    .toList();

            if (rows.isEmpty()) {
                return responderSinResultados(consulta, question,
                        "No encontre esa informacion en tus documentos.", start);
            }

            // Cargar los fragmentos junto con su documento manteniendo el orden de similitud
            List<Long> chunkIds = rows.stream()
                    .map(r -> ((Number) r[0]).longValue())
                    .toList();
            Map<Long, DocumentChunk> chunkById = chunkRepository.findAllByIdWithDocument(chunkIds).stream()
                    .collect(Collectors.toMap(DocumentChunk::getId, Function.identity()));

            // Paso 3: construir contexto numerado y listas de fuentes (DTO + entidad)
            StringBuilder context = new StringBuilder();
            List<QueryResponse.Source> sources = new ArrayList<>();
            List<FuenteRespuesta> fuentesEntidad = new ArrayList<>();

            int pos = 0;
            for (Object[] row : rows) {
                Long chunkId = ((Number) row[0]).longValue();
                DocumentChunk c = chunkById.get(chunkId);
                if (c == null) continue;

                pos++;
                double similitud = clampSimilarity(((Number) row[1]).doubleValue());
                Document d = c.getDocument();

                context.append("[").append(pos).append("] (Documento: ").append(d.getName())
                        .append(d.getGameName() != null ? " | Juego: " + d.getGameName() : "")
                        .append(" | Pagina ").append(c.getPageNumber()).append(")\n")
                        .append(c.getContent()).append("\n\n");

                sources.add(new QueryResponse.Source(
                        pos, d.getId(), d.getName(), d.getGameName(),
                        c.getPageNumber(), excerpt(c.getContent()), similitud));

                fuentesEntidad.add(FuenteRespuesta.builder()
                        .fragmento(c)
                        .documentoNombre(d.getName())
                        .pagina(c.getPageNumber())
                        .similitud(similitud)
                        .posicion(pos)
                        .build());
            }

            String prompt = """
                    Eres GameStats AI, un asistente experto en videojuegos.
                    Responde la PREGUNTA usando UNICAMENTE la informacion del CONTEXTO.
                    Reglas:
                    - Responde en espanol, de forma clara y concisa.
                    - Cita las fuentes con su numero entre corchetes, por ejemplo [1] o [2][3].
                    - Si el contexto no contiene la respuesta, di: "No encontre esa informacion en tus documentos."
                    - No inventes datos, estadisticas ni nombres.

                    CONTEXTO:
                    %s
                    PREGUNTA: %s

                    RESPUESTA:
                    """.formatted(context, question);

            // Paso 4: generacion con el LLM local
            String answer = llmService.generate(prompt);
            long elapsed = System.currentTimeMillis() - start;

            // Paso 5: persistir estado final, respuesta y fuentes
            EstadoConsulta estadoFinal = esSinResultados(answer)
                    ? EstadoConsulta.SIN_RESULTADOS
                    : EstadoConsulta.RESPONDIDA;

            consulta.setEstado(estadoFinal);
            consultaRepository.save(consulta);

            RespuestaIA respuesta = RespuestaIA.builder()
                    .consulta(consulta)
                    .respuesta(answer)
                    .tiempoRespuestaMs(elapsed)
                    .build();
            for (FuenteRespuesta fuente : fuentesEntidad) {
                fuente.setRespuesta(respuesta);
                respuesta.getFuentes().add(fuente);
            }
            respuesta = respuestaRepository.save(respuesta);

            return new QueryResponse(
                    consulta.getId(), respuesta.getId(), estadoFinal.name(),
                    question, answer, sources, elapsed);
        } catch (Exception e) {
            consulta.setEstado(EstadoConsulta.ERROR);
            consulta.setMensajeError(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
            consultaRepository.save(consulta);
            throw e;
        }
    }

    /** Guarda la consulta como SIN_RESULTADOS con un mensaje fijo, sin llamar al LLM. */
    private QueryResponse responderSinResultados(ConsultaIA consulta, String question, String msg, long start) {
        long elapsed = System.currentTimeMillis() - start;
        consulta.setEstado(EstadoConsulta.SIN_RESULTADOS);
        consultaRepository.save(consulta);

        RespuestaIA respuesta = respuestaRepository.save(RespuestaIA.builder()
                .consulta(consulta)
                .respuesta(msg)
                .tiempoRespuestaMs(elapsed)
                .build());

        return new QueryResponse(
                consulta.getId(), respuesta.getId(), consulta.getEstado().name(),
                question, msg, List.of(), elapsed);
    }

    private double clampSimilarity(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value)) return 0.0;
        return Math.max(-1.0, Math.min(1.0, Math.round(value * 10000.0) / 10000.0));
    }

    private boolean esSinResultados(String answer) {
        if (answer == null || answer.isBlank()) return true;
        String normalized = Normalizer.normalize(answer, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase();
        return normalized.contains("no encontre esa informacion");
    }

    private String excerpt(String text) {
        return text.length() <= 300 ? text : text.substring(0, 300) + "...";
    }
}
