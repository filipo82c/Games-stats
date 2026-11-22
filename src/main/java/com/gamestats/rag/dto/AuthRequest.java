package com.gamestats.rag.dto;

import jakarta.validation.constraints.NotBlank;

/** Datos para iniciar sesion. */
public record AuthRequest(
        @NotBlank(message = "El usuario es obligatorio") String username,
        @NotBlank(message = "La contrasena es obligatoria") String password
) {}
