-- ============================================================
-- MIGRACION V1: Tabla de usuarios
-- GameStats AI - Sistema RAG para Videojuegos
-- ============================================================

-- Habilitar la extension para generar UUIDs (identificadores unicos)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Tabla principal de usuarios del sistema
CREATE TABLE users (
    id          BIGSERIAL PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL UNIQUE,     -- Nombre de usuario (unico)
    email       VARCHAR(100) NOT NULL UNIQUE,     -- Email (unico)
    password    VARCHAR(255) NOT NULL,            -- Contrasena encriptada con BCrypt
    role        VARCHAR(20)  NOT NULL DEFAULT 'USER',  -- Rol: USER o ADMIN
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

-- Indices para busquedas rapidas por username y email
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email    ON users(email);
