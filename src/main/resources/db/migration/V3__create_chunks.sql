-- ============================================================
-- MIGRACION V3: Tabla de fragmentos con vectores (pgvector)
-- Esta es la tabla central del sistema RAG
-- ============================================================

-- Habilitar la extension pgvector para almacenar y buscar vectores
CREATE EXTENSION IF NOT EXISTS vector;

-- Tabla de fragmentos de texto con sus embeddings (representaciones vectoriales)
-- Cada fila = un pedazo de texto del PDF + su vector numerico de 768 dimensiones
CREATE TABLE document_chunks (
    id              BIGSERIAL PRIMARY KEY,
    document_id     BIGINT  NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    user_id         BIGINT  NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content         TEXT    NOT NULL,                      -- Texto del fragmento
    embedding       vector(768),                           -- Vector de 768 dimensiones (nomic-embed-text)
    page_number     INTEGER,                               -- Pagina aproximada del PDF
    chunk_index     INTEGER NOT NULL,                      -- Posicion del fragmento en el documento
    embedding_model VARCHAR(50) DEFAULT 'nomic-embed-text', -- Modelo de embedding usado
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indices para busquedas rapidas
CREATE INDEX idx_chunks_document_id ON document_chunks(document_id);
CREATE INDEX idx_chunks_user_id     ON document_chunks(user_id);

-- Indice vectorial HNSW para busqueda de similitud coseno eficiente
-- HNSW = Hierarchical Navigable Small World (algoritmo rapido para vectores)
CREATE INDEX idx_chunks_embedding ON document_chunks
    USING hnsw (embedding vector_cosine_ops)
    WITH (m = 16, ef_construction = 64);
