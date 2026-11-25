-- ============================================================
-- CONSULTAS DEL DASHBOARD - para verificar los numeros a mano
-- Son las mismas que usa DashboardService.java.
--
-- Como correrlas (Docker encendido), desde la carpeta del proyecto:
--   docker exec -it gamestats-db psql -U admin -d gamestats_rag
-- y dentro de psql, primero definir los filtros:
--   \set user_id 1
--   \set desde '2026-11-01'
--   \set hasta '2026-11-30'
-- Luego copiar y pegar cada consulta.
-- (Para ver el id de tu usuario: SELECT id, username FROM users;)
-- ============================================================

-- 1) RESUMEN: total de consultas y cuantas hay de cada estado
SELECT COUNT(*)                                            AS total,
       COUNT(*) FILTER (WHERE estado = 'RESPONDIDA')       AS respondidas,
       COUNT(*) FILTER (WHERE estado = 'SIN_RESULTADOS')   AS sin_resultados,
       COUNT(*) FILTER (WHERE estado = 'ERROR')            AS errores,
       ROUND(100.0 * COUNT(*) FILTER (WHERE estado = 'RESPONDIDA')
             / NULLIF(COUNT(*), 0), 1)                     AS tasa_respuesta_pct
FROM consultas_ia
WHERE user_id = :user_id
  AND created_at >= DATE :'desde'
  AND created_at <  DATE :'hasta' + 1;

-- 2) DISTRIBUCION POR ESTADO (con porcentaje)
SELECT estado, COUNT(*) AS total,
       ROUND(100.0 * COUNT(*) / SUM(COUNT(*)) OVER (), 1) AS porcentaje
FROM consultas_ia
WHERE user_id = :user_id
  AND created_at >= DATE :'desde' AND created_at < DATE :'hasta' + 1
GROUP BY estado
ORDER BY total DESC;

-- 3) TIEMPO PROMEDIO DE RESPUESTA (solo consultas RESPONDIDAS)
SELECT ROUND(AVG(r.tiempo_respuesta_ms)) AS tiempo_promedio_ms
FROM consultas_ia c
JOIN respuestas_ia r ON r.consulta_id = c.id
WHERE c.user_id = :user_id AND c.estado = 'RESPONDIDA'
  AND c.created_at >= DATE :'desde' AND c.created_at < DATE :'hasta' + 1;

-- 4) TENDENCIA DIARIA (incluye dias sin consultas)
SELECT CAST(dia AS DATE) AS fecha, COUNT(c.id) AS total
FROM generate_series(DATE :'desde', DATE :'hasta', INTERVAL '1 day') AS dia
LEFT JOIN consultas_ia c
       ON CAST(c.created_at AS DATE) = CAST(dia AS DATE) AND c.user_id = :user_id
GROUP BY dia
ORDER BY dia;

-- 5) EVOLUCION SEMANAL
SELECT CAST(date_trunc('week', created_at) AS DATE) AS semana,
       COUNT(*) AS total,
       COUNT(*) FILTER (WHERE estado = 'RESPONDIDA') AS respondidas
FROM consultas_ia
WHERE user_id = :user_id
  AND created_at >= DATE :'desde' AND created_at < DATE :'hasta' + 1
GROUP BY semana
ORDER BY semana;

-- 6) CALIFICACIONES: promedio y distribucion de 1 a 5
SELECT ROUND(AVG(cal.puntuacion), 2) AS promedio, COUNT(*) AS total_calificadas
FROM calificaciones cal
JOIN respuestas_ia r ON r.id = cal.respuesta_id
JOIN consultas_ia c  ON c.id = r.consulta_id
WHERE c.user_id = :user_id
  AND c.created_at >= DATE :'desde' AND c.created_at < DATE :'hasta' + 1;

SELECT cal.puntuacion, COUNT(*) AS total
FROM calificaciones cal
JOIN respuestas_ia r ON r.id = cal.respuesta_id
JOIN consultas_ia c  ON c.id = r.consulta_id
WHERE c.user_id = :user_id
  AND c.created_at >= DATE :'desde' AND c.created_at < DATE :'hasta' + 1
GROUP BY cal.puntuacion
ORDER BY cal.puntuacion;

-- 7) JUEGOS MAS CONSULTADOS (segun los documentos citados)
SELECT COALESCE(j.nombre, d.game_name, 'Sin juego') AS juego,
       COUNT(DISTINCT c.id) AS consultas
FROM consultas_ia c
JOIN respuestas_ia r     ON r.consulta_id = c.id
JOIN fuentes_respuesta f ON f.respuesta_id = r.id
JOIN document_chunks dc  ON dc.id = f.fragmento_id
JOIN documents d         ON d.id = dc.document_id
LEFT JOIN juegos j       ON j.id = d.juego_id
WHERE c.user_id = :user_id
  AND c.created_at >= DATE :'desde' AND c.created_at < DATE :'hasta' + 1
GROUP BY 1
ORDER BY consultas DESC
LIMIT 5;

-- 8) DOCUMENTOS MAS CITADOS y su similitud promedio
SELECT f.documento_nombre, COUNT(*) AS veces_citado,
       ROUND(CAST(AVG(f.similitud) AS NUMERIC), 4) AS similitud_promedio
FROM consultas_ia c
JOIN respuestas_ia r     ON r.consulta_id = c.id
JOIN fuentes_respuesta f ON f.respuesta_id = r.id
WHERE c.user_id = :user_id
  AND c.created_at >= DATE :'desde' AND c.created_at < DATE :'hasta' + 1
GROUP BY f.documento_nombre
ORDER BY veces_citado DESC
LIMIT 5;
