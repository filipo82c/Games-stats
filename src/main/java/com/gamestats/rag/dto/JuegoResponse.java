package com.gamestats.rag.dto;

import com.gamestats.rag.model.Juego;

import java.time.LocalDateTime;

/** Representacion de un videojuego devuelta por la API. */
public record JuegoResponse(
        Long id,
        String nombre,
        String genero,
        String descripcion,
        LocalDateTime createdAt
) {
    public static JuegoResponse from(Juego j) {
        return new JuegoResponse(
                j.getId(), j.getNombre(), j.getGenero(), j.getDescripcion(), j.getCreatedAt());
    }
}
