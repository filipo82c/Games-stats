-- ============================================================
-- MIGRACION V2: Tabla de documentos PDF
-- ============================================================

CREATE TABLE documents (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name              VARCHAR(255) NOT NULL,           -- Nombre amigable del documento
    original_filename VARCHAR(255) NOT NULL,           -- Nombre original del archivo PDF
    file_hash         VARCHAR(64),                    -- Hash SHA-256 para verificar integridad
    game_name         VARCHAR(100),                   -- Nombre del videojuego (opcional)
    status            VARCHAR(20)  NOT NULL DEFAULT 'PROCESSING',  -- Estado: PROCESSING, READY, ERROR
    total_chunks      INTEGER      DEFAULT 0,          -- Cuantos fragmentos se generaron
    total_pages       INTEGER      DEFAULT 0,          -- Cuantas paginas tenia el PDF
    error_message     TEXT,                           -- Mensaje de error si el procesamiento fallo
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- El PDF NUNCA se guarda permanentemente en el servidor
-- Solo se almacenan los fragmentos de texto y sus vectores

CREATE INDEX idx_documents_user_id ON documents(user_id);
CREATE INDEX idx_documents_status  ON documents(status);
