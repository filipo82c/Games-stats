package com.gamestats.rag.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Datos para calificar una respuesta de la IA (puntuacion de 1 a 5). */
public record CalificacionRequest(
        @NotNull(message = "La puntuacion es obligatoria")
        @Min(value = 1, message = "La puntuacion debe estar entre 1 y 5")
        @Max(value = 5, message = "La puntuacion debe estar entre 1 y 5")
        Short puntuacion,

        String comentario
) {}
