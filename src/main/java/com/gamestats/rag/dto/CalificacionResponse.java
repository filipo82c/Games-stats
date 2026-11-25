package com.gamestats.rag.dto;

import com.gamestats.rag.model.Calificacion;

import java.time.LocalDateTime;

/** Representacion de la calificacion de una respuesta de la IA. */
public record CalificacionResponse(
        Long id,
        Long respuestaId,
        Short puntuacion,
        String comentario,
        LocalDateTime createdAt
) {
    public static CalificacionResponse from(Calificacion c) {
        return new CalificacionResponse(
                c.getId(), c.getRespuesta().getId(), c.getPuntuacion(),
                c.getComentario(), c.getCreatedAt());
    }
}
