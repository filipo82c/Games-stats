package com.gamestats.rag.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad que guarda cada pregunta que un usuario le hace a la IA.
 * Permite tener el historial de consultas.
 */
@Entity
@Table(name = "consultas_ia")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaIA {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private User user;  // Quien hizo la pregunta

    @Column(nullable = false, columnDefinition = "TEXT")
    private String pregunta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoConsulta estado;

    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError;  // Solo se llena si el estado es ERROR

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "consulta", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RespuestaIA respuesta;  // null si la consulta quedo en ERROR o PENDIENTE

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (estado == null) estado = EstadoConsulta.PENDIENTE;
    }
}
