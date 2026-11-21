package com.gamestats.rag.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un fragmento de texto de un PDF con su embedding vectorial.
 *
 * Esta es la tabla mas importante del sistema RAG.
 * Cada fragmento tiene:
 * - content: el texto en si (500-1000 palabras)
 * - embedding: su representacion numerica como vector de 768 dimensiones
 *
 * Los vectores permiten buscar fragmentos semanticamente similares a una pregunta.
 */
@Entity
@Table(name = "document_chunks")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunk {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;  // A que documento pertenece este fragmento

    @Column(name = "user_id", nullable = false)
    private Long userId;  // ID del usuario (para seguridad multi-tenant)

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;  // El texto del fragmento

    // El embedding se almacena como texto en formato pgvector: "[0.1, 0.2, ...]"
    // PostgreSQL lo interpreta como vector(768) gracias al tipo de columna
    @Column(columnDefinition = "vector(768)")
    private String embedding;

    @Column(name = "page_number")
    private Integer pageNumber;  // Pagina aproximada del PDF de donde viene

    @Column(name = "chunk_index", nullable = false)
    private Integer chunkIndex;  // Posicion del fragmento dentro del documento

    @Column(name = "embedding_model", length = 50)
    private String embeddingModel;  // Modelo usado para generar el embedding

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (embeddingModel == null) embeddingModel = "nomic-embed-text";
    }
}
