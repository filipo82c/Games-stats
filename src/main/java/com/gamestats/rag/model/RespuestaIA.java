package com.gamestats.rag.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad que guarda la respuesta que genero la IA para una consulta.
 * Relacion 1 a 1: cada consulta tiene como maximo una respuesta.
 */
@Entity
@Table(name = "respuestas_ia")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RespuestaIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "consulta_id", nullable = false, unique = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private ConsultaIA consulta;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String respuesta;  // Texto que genero el modelo

    @Column(name = "tiempo_respuesta_ms")
    private Long tiempoRespuestaMs;  // Cuanto tardo en responder

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // Fragmentos usados como fuente. Al guardar la respuesta se guardan tambien sus fuentes
    @OneToMany(mappedBy = "respuesta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("posicion ASC")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<FuenteRespuesta> fuentes = new ArrayList<>();

    @OneToOne(mappedBy = "respuesta", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Calificacion calificacion;  // null hasta que el usuario califique

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
