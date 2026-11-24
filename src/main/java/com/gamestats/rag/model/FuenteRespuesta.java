package com.gamestats.rag.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entidad que guarda cada fragmento que la IA uso como fuente en una respuesta.
 * Asi se puede verificar de donde salio cada dato.
 */
@Entity
@Table(name = "fuentes_respuesta")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FuenteRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respuesta_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private RespuestaIA respuesta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fragmento_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private DocumentChunk fragmento;  // Puede quedar null si se borro el documento

    @Column(name = "documento_nombre", nullable = false, length = 255)
    private String documentoNombre;  // Copia del nombre para no perder el historial

    private Integer pagina;

    @Column(nullable = false)
    private Double similitud;  // 1 = identico, 0 = nada que ver

    @Column(nullable = false)
    private Integer posicion;  // El numero con el que se cito: [1], [2]...
}
