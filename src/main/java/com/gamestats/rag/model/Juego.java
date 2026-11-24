package com.gamestats.rag.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que representa un videojuego.
 * Sirve para agrupar los documentos (equivale a "Categoria" del enunciado).
 */
@Entity
@Table(name = "juegos")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Juego {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;  // Nombre del juego, no se puede repetir

    @Column(length = 50)
    private String genero;  // Ej: MOBA, Shooter, RPG

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
