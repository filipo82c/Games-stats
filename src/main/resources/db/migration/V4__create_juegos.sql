-- ============================================================
-- MIGRACION V4: Tabla de juegos (equivale a "Categoria")
-- Cada documento puede pertenecer a un juego
-- ============================================================

CREATE TABLE juegos (
    id          BIGSERIAL    PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL UNIQUE,   -- Nombre del juego (unico)
    genero      VARCHAR(50),                    -- Ej: MOBA, Shooter, RPG
    descripcion TEXT,
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Relacion: un juego tiene muchos documentos (opcional, por eso permite NULL)
-- ON DELETE SET NULL: si se borra el juego, el documento queda sin juego pero no se pierde
ALTER TABLE documents
    ADD COLUMN juego_id BIGINT REFERENCES juegos(id) ON DELETE SET NULL;

CREATE INDEX idx_documents_juego_id ON documents(juego_id);
