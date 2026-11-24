package com.gamestats.rag.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que guarda la calificacion (1 a 5) que el usuario le da a una respuesta.
 * Relacion 1 a 1: cada respuesta se califica una sola vez.
 */
@Entity
@Table(name = "calificaciones")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Calificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respuesta_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RespuestaIA respuesta;

    @Column(nullable = false)
    private Short puntuacion;  // De 1 a 5 (la base de datos lo valida con un CHECK)

    @Column(columnDefinition = "TEXT")
    private String comentario;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
