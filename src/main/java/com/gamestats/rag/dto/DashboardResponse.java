package com.gamestats.rag.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * Indicadores del dashboard. TODOS se calculan con consultas SQL sobre la base de datos
 * (ver DashboardService y docs/consultas_dashboard.sql): ninguna cifra la inventa la IA.
 */
public record DashboardResponse(
        Filtros filtros,
        Resumen resumen,
        List<ConteoEstado> porEstado,
        List<ConteoDia> porDia,
        List<ConteoSemana> porSemana,
        List<ConteoCalificacion> calificaciones,
        List<RankingJuego> topJuegos,
        List<RankingDocumento> topDocumentos
) {
    /** Filtros que se aplicaron (para mostrarlos en pantalla). */
    public record Filtros(LocalDate desde, LocalDate hasta, Long juegoId) {}

    /** Tarjetas de arriba del dashboard. */
    public record Resumen(
            long totalConsultas,
            long respondidas,
            long sinResultados,
            long errores,
            long pendientes,
            Double tasaRespuesta,          // % de consultas RESPONDIDAS
            Double tiempoPromedioMs,       // Tiempo promedio de las RESPONDIDAS
            Double calificacionPromedio,   // Promedio de 1 a 5
            long totalCalificadas,
            Double similitudPromedio,      // Similitud promedio de las fuentes usadas
            long documentosListos,         // Documentos en estado READY (sin filtro de fecha)
            long totalFragmentos           // Fragmentos indexados (sin filtro de fecha)
    ) {}

    public record ConteoEstado(String estado, long total, Double porcentaje) {}

    public record ConteoDia(LocalDate fecha, long total, long respondidas, long sinResultados, long errores) {}

    public record ConteoSemana(LocalDate semana, long total, long respondidas, Double tasaRespuesta) {}

    public record ConteoCalificacion(int puntuacion, long total) {}

    public record RankingJuego(String juego, long consultas) {}

    public record RankingDocumento(String documento, long vecesCitado, Double similitudPromedio) {}
}
