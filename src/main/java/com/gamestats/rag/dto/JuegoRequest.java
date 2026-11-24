package com.gamestats.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos para crear o actualizar un videojuego. */
public record JuegoRequest(
        @NotBlank(message = "El nombre del juego es obligatorio")
        @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
        String nombre,

        @Size(max = 50, message = "El genero no puede superar 50 caracteres")
        String genero,

        String descripcion
) {}
