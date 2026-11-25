package com.gamestats.rag.service;

import com.gamestats.rag.dto.DashboardResponse;
import com.gamestats.rag.dto.DashboardResponse.*;
import com.gamestats.rag.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Modulo de analisis de datos. Calcula los indicadores del dashboard con SQL real.
 *
 * Todas las consultas parten del mismo filtro "base" (CTE):
 *  - solo las consultas del usuario autenticado (multi-tenant)
 *  - dentro del rango de fechas [desde, hasta]
 *  - opcionalmente, solo las que citaron documentos de un juego
 *
 * Las mismas consultas estan en docs/consultas_dashboard.sql para verificarlas a mano.
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final int MAX_DIAS = 366;

    private final NamedParameterJdbcTemplate jdbc;

    // Filtro comun. CAST(:juegoId AS BIGINT) evita que PostgreSQL falle cuando el parametro es NULL
    private static final String BASE = """
            WITH base AS (
                SELECT c.*
                FROM consultas_ia c
                WHERE c.user_id = :userId
                  AND c.created_at >= :desde
                  AND c.created_at <  :hastaExclusivo
                  AND (CAST(:juegoId AS BIGINT) IS NULL OR EXISTS (
                        SELECT 1
                        FROM respuestas_ia r
                        JOIN fuentes_respuesta f ON f.respuesta_id = r.id
                        JOIN document_chunks dc  ON dc.id = f.fragmento_id
                        JOIN documents d         ON d.id = dc.document_id
                        WHERE r.consulta_id = c.id
                          AND d.juego_id = CAST(:juegoId AS BIGINT)))
            )
            """;

    public DashboardResponse calcular(User user, LocalDate desde, LocalDate hasta, Long juegoId) {
        // Por defecto: los ultimos 30 dias
        if (hasta == null) hasta = LocalDate.now();
        if (desde == null) desde = hasta.minusDays(29);
        if (desde.isAfter(hasta)) {
            throw new IllegalArgumentException("La fecha 'desde' no puede ser mayor que 'hasta'");
        }
        if (ChronoUnit.DAYS.between(desde, hasta) >= MAX_DIAS) {
            throw new IllegalArgumentException("El rango maximo es de " + MAX_DIAS + " dias");
        }

        MapSqlParameterSource p = new MapSqlParameterSource()
                .addValue("userId", user.getId())
                .addValue("desde", Timestamp.valueOf(desde.atStartOfDay()))
                .addValue("hastaExclusivo", Timestamp.valueOf(hasta.plusDays(1).atStartOfDay()))
                .addValue("desdeFecha", java.sql.Date.valueOf(desde))
                .addValue("hastaFecha", java.sql.Date.valueOf(hasta))
                .addValue("juegoId", juegoId, Types.BIGINT);

        return new DashboardResponse(
                new Filtros(desde, hasta, juegoId),
                resumen(p),
                porEstado(p),
                porDia(p),
                porSemana(p),
                calificaciones(p),
                topJuegos(p),
                topDocumentos(p));
    }

    // ---------- Indicadores ----------

    private Resumen resumen(MapSqlParameterSource p) {
        String sql = BASE + """
                SELECT
                    COUNT(*)                                              AS total,
                    COUNT(*) FILTER (WHERE b.estado = 'RESPONDIDA')       AS respondidas,
                    COUNT(*) FILTER (WHERE b.estado = 'SIN_RESULTADOS')   AS sin_resultados,
                    COUNT(*) FILTER (WHERE b.estado = 'ERROR')            AS errores,
                    COUNT(*) FILTER (WHERE b.estado = 'PENDIENTE')        AS pendientes,
                    ROUND(100.0 * COUNT(*) FILTER (WHERE b.estado = 'RESPONDIDA')
                          / NULLIF(COUNT(*), 0), 1)                       AS tasa_respuesta,
                    (SELECT ROUND(AVG(r.tiempo_respuesta_ms), 0)
                       FROM base b2 JOIN respuestas_ia r ON r.consulta_id = b2.id
                      WHERE b2.estado = 'RESPONDIDA')                     AS tiempo_promedio_ms,
                    (SELECT ROUND(AVG(cal.puntuacion), 2)
                       FROM base b2 JOIN respuestas_ia r ON r.consulta_id = b2.id
                       JOIN calificaciones cal ON cal.respuesta_id = r.id) AS calificacion_promedio,
                    (SELECT COUNT(*)
                       FROM base b2 JOIN respuestas_ia r ON r.consulta_id = b2.id
                       JOIN calificaciones cal ON cal.respuesta_id = r.id) AS total_calificadas,
                    (SELECT ROUND(CAST(AVG(f.similitud) AS NUMERIC), 4)
                       FROM base b2 JOIN respuestas_ia r ON r.consulta_id = b2.id
                       JOIN fuentes_respuesta f ON f.respuesta_id = r.id) AS similitud_promedio,
                    (SELECT COUNT(*) FROM documents d
                      WHERE d.user_id = :userId AND d.status = 'READY')   AS documentos_listos,
                    (SELECT COUNT(*) FROM document_chunks dc
                      WHERE dc.user_id = :userId)                         AS total_fragmentos
                FROM base b
                """;
        return jdbc.queryForObject(sql, p, (rs, i) -> new Resumen(
                rs.getLong("total"),
                rs.getLong("respondidas"),
                rs.getLong("sin_resultados"),
                rs.getLong("errores"),
                rs.getLong("pendientes"),
                dbl(rs, "tasa_respuesta"),
                dbl(rs, "tiempo_promedio_ms"),
                dbl(rs, "calificacion_promedio"),
                rs.getLong("total_calificadas"),
                dbl(rs, "similitud_promedio"),
                rs.getLong("documentos_listos"),
                rs.getLong("total_fragmentos")));
    }

    private List<ConteoEstado> porEstado(MapSqlParameterSource p) {
        String sql = BASE + """
                SELECT b.estado,
                       COUNT(*) AS total,
                       ROUND(100.0 * COUNT(*) / NULLIF(SUM(COUNT(*)) OVER (), 0), 1) AS porcentaje
                FROM base b
                GROUP BY b.estado
                ORDER BY total DESC
                """;
        return jdbc.query(sql, p, (rs, i) -> new ConteoEstado(
                rs.getString("estado"), rs.getLong("total"), dbl(rs, "porcentaje")));
    }

    /** Tendencia diaria. generate_series incluye los dias sin consultas (con 0). */
    private List<ConteoDia> porDia(MapSqlParameterSource p) {
        String sql = BASE + """
                SELECT CAST(dia AS DATE) AS fecha,
                       COUNT(b.id)                                          AS total,
                       COUNT(b.id) FILTER (WHERE b.estado = 'RESPONDIDA')     AS respondidas,
                       COUNT(b.id) FILTER (WHERE b.estado = 'SIN_RESULTADOS') AS sin_resultados,
                       COUNT(b.id) FILTER (WHERE b.estado = 'ERROR')          AS errores
                FROM generate_series(CAST(:desdeFecha AS DATE), CAST(:hastaFecha AS DATE),
                                     INTERVAL '1 day') AS dia
                LEFT JOIN base b ON CAST(b.created_at AS DATE) = CAST(dia AS DATE)
                GROUP BY dia
                ORDER BY dia
                """;
        return jdbc.query(sql, p, (rs, i) -> new ConteoDia(
                rs.getDate("fecha").toLocalDate(),
                rs.getLong("total"),
                rs.getLong("respondidas"),
                rs.getLong("sin_resultados"),
                rs.getLong("errores")));
    }

    /** Evolucion semanal (semana que empieza el lunes). */
    private List<ConteoSemana> porSemana(MapSqlParameterSource p) {
        String sql = BASE + """
                SELECT CAST(date_trunc('week', b.created_at) AS DATE)    AS semana,
                       COUNT(*)                                          AS total,
                       COUNT(*) FILTER (WHERE b.estado = 'RESPONDIDA')   AS respondidas,
                       ROUND(100.0 * COUNT(*) FILTER (WHERE b.estado = 'RESPONDIDA')
                             / NULLIF(COUNT(*), 0), 1)                   AS tasa_respuesta
                FROM base b
                GROUP BY semana
                ORDER BY semana
                """;
        return jdbc.query(sql, p, (rs, i) -> new ConteoSemana(
                rs.getDate("semana").toLocalDate(),
                rs.getLong("total"),
                rs.getLong("respondidas"),
                dbl(rs, "tasa_respuesta")));
    }

    /** Distribucion de calificaciones 1..5 (incluye las puntuaciones sin votos, con 0). */
    private List<ConteoCalificacion> calificaciones(MapSqlParameterSource p) {
        String sql = BASE + """
                SELECT n AS puntuacion, COUNT(cal.id) AS total
                FROM generate_series(1, 5) AS n
                LEFT JOIN (
                    SELECT cal.id, cal.puntuacion
                    FROM base b
                    JOIN respuestas_ia r    ON r.consulta_id = b.id
                    JOIN calificaciones cal ON cal.respuesta_id = r.id
                ) cal ON cal.puntuacion = n
                GROUP BY n
                ORDER BY n
                """;
        return jdbc.query(sql, p, (rs, i) -> new ConteoCalificacion(
                rs.getInt("puntuacion"), rs.getLong("total")));
    }

    /** Juegos con mas consultas (segun los documentos que se citaron como fuente). */
    private List<RankingJuego> topJuegos(MapSqlParameterSource p) {
        String sql = BASE + """
                SELECT COALESCE(j.nombre, d.game_name, 'Sin juego') AS juego,
                       COUNT(DISTINCT b.id)                         AS consultas
                FROM base b
                JOIN respuestas_ia r     ON r.consulta_id = b.id
                JOIN fuentes_respuesta f ON f.respuesta_id = r.id
                JOIN document_chunks dc  ON dc.id = f.fragmento_id
                JOIN documents d         ON d.id = dc.document_id
                LEFT JOIN juegos j       ON j.id = d.juego_id
                GROUP BY COALESCE(j.nombre, d.game_name, 'Sin juego')
                ORDER BY consultas DESC
                LIMIT 5
                """;
        return jdbc.query(sql, p, (rs, i) -> new RankingJuego(
                rs.getString("juego"), rs.getLong("consultas")));
    }

    /** Documentos mas citados y su similitud promedio. */
    private List<RankingDocumento> topDocumentos(MapSqlParameterSource p) {
        String sql = BASE + """
                SELECT f.documento_nombre                                  AS documento,
                       COUNT(*)                                            AS veces_citado,
                       ROUND(CAST(AVG(f.similitud) AS NUMERIC), 4)         AS similitud_promedio
                FROM base b
                JOIN respuestas_ia r     ON r.consulta_id = b.id
                JOIN fuentes_respuesta f ON f.respuesta_id = r.id
                GROUP BY f.documento_nombre
                ORDER BY veces_citado DESC
                LIMIT 5
                """;
        return jdbc.query(sql, p, (rs, i) -> new RankingDocumento(
                rs.getString("documento"), rs.getLong("veces_citado"), dbl(rs, "similitud_promedio")));
    }

    /** Lee un numero que puede venir NULL (por ejemplo, AVG sin filas). */
    private static Double dbl(ResultSet rs, String col) throws SQLException {
        Object v = rs.getObject(col);
        return v == null ? null : ((Number) v).doubleValue();
    }
}
