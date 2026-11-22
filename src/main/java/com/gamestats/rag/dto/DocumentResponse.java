package com.gamestats.rag.dto;

import com.gamestats.rag.model.Document;

import java.time.LocalDateTime;

/** Informacion de un documento que se devuelve al frontend (sin exponer la entidad). */
public record DocumentResponse(
        Long id,
        String name,
        String originalFilename,
        String gameName,
        String status,
        Integer totalChunks,
        Integer totalPages,
        String errorMessage,
        LocalDateTime createdAt
) {
    public static DocumentResponse from(Document d) {
        return new DocumentResponse(
                d.getId(), d.getName(), d.getOriginalFilename(), d.getGameName(),
                d.getStatus().name(), d.getTotalChunks(), d.getTotalPages(),
                d.getErrorMessage(), d.getCreatedAt());
    }
}
