-- ============================================================
-- MIGRACION V6: Fuentes de cada respuesta y calificaciones
-- ============================================================

-- Que fragmentos uso la IA para cada respuesta, y que tan parecidos eran
CREATE TABLE fuentes_respuesta (
    id                BIGSERIAL    PRIMARY KEY,
    respuesta_id      BIGINT       NOT NULL REFERENCES respuestas_ia(id) ON DELETE CASCADE,
    -- SET NULL: si se borra el documento, el historial se conserva
    fragmento_id      BIGINT       REFERENCES document_chunks(id) ON DELETE SET NULL,
    documento_nombre  VARCHAR(255) NOT NULL,   -- Copia del nombre, por si el documento se borra
    pagina            INTEGER,
    similitud         DOUBLE PRECISION NOT NULL,
    posicion          INTEGER      NOT NULL,   -- 1 = [1], 2 = [2]... como se cito en la respuesta
    CONSTRAINT chk_fuentes_similitud CHECK (similitud BETWEEN -1 AND 1)
);

CREATE INDEX idx_fuentes_respuesta_id ON fuentes_respuesta(respuesta_id);

-- Calificacion de 1 a 5 que el usuario le da a una respuesta (una por respuesta)
CREATE TABLE calificaciones (
    id            BIGSERIAL PRIMARY KEY,
    respuesta_id  BIGINT    NOT NULL UNIQUE REFERENCES respuestas_ia(id) ON DELETE CASCADE,
    puntuacion    SMALLINT  NOT NULL,
    comentario    TEXT,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_calificacion_puntuacion CHECK (puntuacion BETWEEN 1 AND 5)
);
