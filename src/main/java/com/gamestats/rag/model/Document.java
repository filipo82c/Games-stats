package com.gamestats.rag.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un documento PDF subido por el usuario.
 * El PDF original NUNCA se guarda en el servidor; solo los fragmentos de texto.
 */
@Entity
@Table(name = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // El usuario que subio este documento

    @Column(nullable = false, length = 255)
    private String name;  // Nombre amigable que el usuario le dio

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;  // Nombre original del archivo PDF

    @Column(name = "file_hash", length = 64)
    private String fileHash;  // Hash SHA-256 para verificar integridad del archivo

    @Column(name = "game_name", length = 100)
    private String gameName;  // Nombre del videojuego (opcional, para organizacion)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DocumentStatus status;  // Estado del procesamiento

    @Column(name = "total_chunks")
    private Integer totalChunks;  // Cuantos fragmentos se generaron

    @Column(name = "total_pages")
    private Integer totalPages;  // Cuantas paginas tenia el PDF

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;  // Mensaje de error si el procesamiento fallo

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) status = DocumentStatus.PROCESSING;
        if (totalChunks == null) totalChunks = 0;
        if (totalPages == null) totalPages = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
