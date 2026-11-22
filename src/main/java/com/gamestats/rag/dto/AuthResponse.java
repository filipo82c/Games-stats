package com.gamestats.rag.dto;

/** Respuesta de login/registro: el token JWT que el frontend debe enviar en cada peticion. */
public record AuthResponse(String token, String username, String role) {}
