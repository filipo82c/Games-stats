package com.gamestats.rag.dto;

import com.gamestats.rag.model.Calificacion;
import com.gamestats.rag.model.ConsultaIA;
import com.gamestats.rag.model.FuenteRespuesta;
import com.gamestats.rag.model.RespuestaIA;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Elemento del historial de consultas a la IA con su respuesta, fuentes y calificacion.
 */
public record ConsultaHistorialResponse(
        Long id,
        String pregunta,
        String estado,
        String mensajeError,
        LocalDateTime createdAt,
        RespuestaResumen respuesta
) {
    public record RespuestaResumen(
            Long id,
            String respuesta,
            Long tiempoRespuestaMs,
            LocalDateTime createdAt,
            List<FuenteResumen> fuentes,
            CalificacionResumen calificacion
    ) {}

    public record FuenteResumen(
            Long id,
            Integer posicion,
            String documentoNombre,
            Integer pagina,
            Double similitud
    ) {}

    public record CalificacionResumen(
            Long id,
            Short puntuacion,
            String comentario,
            LocalDateTime createdAt
    ) {}

    public static ConsultaHistorialResponse from(ConsultaIA c) {
        RespuestaResumen resp = null;
        RespuestaIA r = c.getRespuesta();
        if (r != null) {
            List<FuenteResumen> fuentes = r.getFuentes().stream()
                    .map(ConsultaHistorialResponse::mapFuente)
                    .toList();
            Calificacion cal = r.getCalificacion();
            CalificacionResumen calResumen = cal == null ? null : new CalificacionResumen(
                    cal.getId(), cal.getPuntuacion(), cal.getComentario(), cal.getCreatedAt());

            resp = new RespuestaResumen(
                    r.getId(), r.getRespuesta(), r.getTiempoRespuestaMs(),
                    r.getCreatedAt(), fuentes, calResumen);
        }

        return new ConsultaHistorialResponse(
                c.getId(), c.getPregunta(), c.getEstado().name(),
                c.getMensajeError(), c.getCreatedAt(), resp);
    }

    private static FuenteResumen mapFuente(FuenteRespuesta f) {
        return new FuenteResumen(
                f.getId(), f.getPosicion(), f.getDocumentoNombre(), f.getPagina(), f.getSimilitud());
    }
}
