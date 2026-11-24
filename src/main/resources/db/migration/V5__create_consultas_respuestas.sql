-- ============================================================
-- MIGRACION V5: Historial de consultas a la IA y sus respuestas
-- ============================================================

-- Cada pregunta que hace un usuario queda guardada
CREATE TABLE consultas_ia (
    id             BIGSERIAL   PRIMARY KEY,
    user_id        BIGINT      NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    pregunta       TEXT        NOT NULL,
    estado         VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    mensaje_error  TEXT,                        -- Solo se llena si estado = ERROR
    created_at     TIMESTAMP   NOT NULL DEFAULT NOW(),
    -- Solo se permiten los 4 estados definidos
    CONSTRAINT chk_consultas_estado
        CHECK (estado IN ('PENDIENTE', 'RESPONDIDA', 'SIN_RESULTADOS', 'ERROR'))
);

CREATE INDEX idx_consultas_user_id ON consultas_ia(user_id);
CREATE INDEX idx_consultas_estado  ON consultas_ia(estado);
CREATE INDEX idx_consultas_fecha   ON consultas_ia(created_at);

-- La respuesta generada por la IA (una por consulta)
CREATE TABLE respuestas_ia (
    id                BIGSERIAL PRIMARY KEY,
    consulta_id       BIGINT    NOT NULL UNIQUE REFERENCES consultas_ia(id) ON DELETE CASCADE,
    respuesta         TEXT      NOT NULL,
    tiempo_respuesta_ms BIGINT,                  -- Cuanto tardo el modelo en responder
    created_at        TIMESTAMP NOT NULL DEFAULT NOW()
);
