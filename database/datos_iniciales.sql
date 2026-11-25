-- ============================================================
-- DATOS INICIALES - GameStats AI
-- Se ejecuta DESPUES de que la app arranco (Flyway ya creo las tablas).
-- Se puede correr varias veces: ON CONFLICT evita duplicados.
--
-- Como ejecutarlo (con Docker encendido), desde la carpeta del proyecto:
--   Get-Content database\datos_iniciales.sql | docker exec -i gamestats-db psql -U admin -d gamestats_rag
-- ============================================================

-- Usuario de prueba para la demo
--   usuario: demo     contrasena: demo1234
-- La contrasena esta guardada con BCrypt (nunca en texto plano)
INSERT INTO users (username, email, password, role)
VALUES ('demo', 'demo@gamestats.local',
        '$2a$10$bni.PqbmDTtJuP9RMEpPdeuOjv1V3u9NEgBod/r08m0qNQ9CETn/2', 'USER')
ON CONFLICT (username) DO NOTHING;

-- Juegos (equivalen a las "categorias" del enunciado)
INSERT INTO juegos (nombre, genero, descripcion) VALUES
  ('Ecos de Valdoria', 'MOBA',
   'Juego ficticio usado en la demo. Su PDF de prueba son las notas del parche 4.7 "Marea Helada".'),
  ('Neon Strike', 'Shooter',
   'Juego ficticio usado en las pruebas de Antigravity (notas del parche 7.4).'),
  ('League of Legends', 'MOBA', 'Juego real, para subir notas de parche oficiales.'),
  ('Valorant', 'Shooter tactico', 'Juego real, para subir notas de parche oficiales.'),
  ('Elden Ring', 'RPG de accion', 'Juego real, para subir guias y estadisticas de armas.')
ON CONFLICT (nombre) DO NOTHING;

-- Verificacion rapida: deberia mostrar 5 juegos y el usuario demo
SELECT 'juegos' AS tabla, COUNT(*) AS total FROM juegos
UNION ALL
SELECT 'usuario demo', COUNT(*) FROM users WHERE username = 'demo';
