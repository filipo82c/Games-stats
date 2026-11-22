package com.gamestats.rag.service;

import com.gamestats.rag.dto.QueryResponse;
import com.gamestats.rag.model.Document;
import com.gamestats.rag.model.DocumentChunk;
import com.gamestats.rag.model.User;
import com.gamestats.rag.repository.DocumentChunkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * EL CORAZON DEL RAG. Responde preguntas en 4 pasos:
 *
 *  1. RETRIEVAL   - Convertir la pregunta en embedding
 *  2. RETRIEVAL   - Buscar en pgvector los K fragmentos mas parecidos (solo del usuario)
 *  3. AUGMENTED   - Armar un prompt con esos fragmentos numerados como contexto
 *  4. GENERATION  - El LLM responde usando SOLO ese contexto y citando [1], [2]...
 */
@Service
@RequiredArgsConstructor
public class RagQueryService {

    private final EmbeddingService embeddingService;
    private final DocumentChunkRepository chunkRepository;
    private final OllamaLlmService llmService;

    @Value("${app.rag.top-k}")
    private int topK;

    @Transactional(readOnly = true)
    public QueryResponse ask(String question, User user) {
        long start = System.currentTimeMillis();

        // Pasos 1 y 2: embedding de la pregunta + busqueda semantica
        String queryEmbedding = embeddingService.embed(question);
        List<DocumentChunk> chunks = chunkRepository.findSimilarChunks(user.getId(), queryEmbedding, topK);

        if (chunks.isEmpty()) {
            return new QueryResponse(question,
                    "No tienes documentos indexados todavia. Sube un PDF de un videojuego para empezar.",
                    List.of(), System.currentTimeMillis() - start);
        }

        // Paso 3: construir contexto numerado y la lista de fuentes
        StringBuilder context = new StringBuilder();
        List<QueryResponse.Source> sources = new ArrayList<>();
        for (int i = 0; i < chunks.size(); i++) {
            DocumentChunk c = chunks.get(i);
            Document d = c.getDocument();
            int n = i + 1;
            context.append("[").append(n).append("] (Documento: ").append(d.getName())
                    .append(d.getGameName() != null ? " | Juego: " + d.getGameName() : "")
                    .append(" | Pagina ").append(c.getPageNumber()).append(")\n")
                    .append(c.getContent()).append("\n\n");
            sources.add(new QueryResponse.Source(n, d.getId(), d.getName(), d.getGameName(),
                    c.getPageNumber(), excerpt(c.getContent())));
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

        // Paso 4: generacion
        String answer = llmService.generate(prompt);
        return new QueryResponse(question, answer, sources, System.currentTimeMillis() - start);
    }

    private String excerpt(String text) {
        return text.length() <= 300 ? text : text.substring(0, 300) + "...";
    }
}
