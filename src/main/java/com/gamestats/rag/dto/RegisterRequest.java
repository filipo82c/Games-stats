package com.gamestats.rag.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Datos para registrar un usuario nuevo. */
public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50, message = "El usuario debe tener entre 3 y 50 caracteres") String username,
        @NotBlank @Email(message = "Email invalido") String email,
        @NotBlank @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres") String password
) {}
