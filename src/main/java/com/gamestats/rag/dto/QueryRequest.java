package com.gamestats.rag.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Pregunta del usuario sobre sus documentos. */
public record QueryRequest(
        @NotBlank(message = "La pregunta no puede estar vacia")
        @Size(max = 1000, message = "La pregunta es demasiado larga")
        String question
) {}
