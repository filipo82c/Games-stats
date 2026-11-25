# Uso de Inteligencia Artificial en GameStats AI

Este documento explica qué herramientas de IA usé en el proyecto, en qué partes ayudaron y qué decidí o hice yo.

> **Pendiente de completar por Filip:** revisar cada sección y llenar lo que está entre corchetes `[ ]`. Este documento debe reflejar lo que realmente pasó.

## 1. Herramientas utilizadas

| Herramienta | Tipo | Para qué la usé |
|---|---|---|
| **Antigravity** (Google) | Agente de programación con acceso a mi PC | Escribir, compilar, ejecutar y corregir el código |
| **Claude** (Anthropic) | Asistente conversacional con acceso a la carpeta del proyecto | Diseño del modelo de datos, entidades, documentación, presentación y ensayo de la exposición |
| **Ollama** (`nomic-embed-text`, `llama3.2:3b`) | Modelos de IA locales | Son parte **del producto**: generan los embeddings y las respuestas del RAG. No ayudaron a desarrollar |

## 2. Participación por componente

| Componente | Quién lo hizo | Detalle |
|---|---|---|
| Idea del proyecto y alcance | Filip | Elegí el tema (RAG sobre PDFs de videojuegos) y el stack |
| Código base (backend, frontend, migraciones V1–V3) | [Filip / Antigravity: completar] | [Describir quién escribió qué] |
| Corrección de compatibilidad | Antigravity | Subió Spring Boot a 3.5.7 y Lombok a 1.18.42 para que funcionara con Java 25; declaró Lombok en el `pom.xml` |
| Configuración del entorno | Antigravity | Instaló Maven, cambió PostgreSQL al puerto 5440 (5432 y 5433 estaban ocupados por otro proyecto) |
| Pruebas iniciales | Antigravity | Probó registro, login, 401 sin token / contraseña incorrecta, listado y subida de PDF |
| PDF de demo "Neon Strike 7.4" | Antigravity | `demo/GenDemoPdf.java` |
| PDF de demo "Ecos de Valdoria 4.7" y preguntas de prueba | Claude | Patch notes ficticias con números verificables |
| Modelo de datos ampliado (8 entidades) | Claude, a partir de los requisitos del enunciado | `Juego`, `ConsultaIA`, `EstadoConsulta`, `RespuestaIA`, `FuenteRespuesta`, `Calificacion`, relación `Document → Juego`, migraciones V4–V6 y 5 repositorios |
| Conexión de las entidades con `RagQueryService` y endpoints de historial, calificación y juegos | Antigravity | Persistencia de `ConsultaIA`, `RespuestaIA` y `FuenteRespuesta` con similitud y estados; endpoints `/api/juegos`, `/api/consultas` y `/api/respuestas/{id}/calificacion` |
| Colección de Postman, `.env.example`, script de datos iniciales | Claude | Carpeta `postman/`, archivo `.env.example`, `database/datos_iniciales.sql` |
| Variables de entorno en `application.yml` | Claude | Las credenciales se leen de variables de entorno, con valores por defecto para desarrollo |
| Presentación (10 diapositivas con notas) | Claude | [Indicar cambios que hice yo] |
| Instalación en el PC de escritorio (Docker, WSL, Ollama, Java, Maven) | Filip, guiado por Claude | |
| Revisión, pruebas y exposición | Filip | [Completar] |

## 3. Decisiones técnicas tomadas con apoyo de IA

1. **pgvector en lugar de Qdrant**: guardar los vectores en la misma base PostgreSQL evita un servicio extra. Confirmé con el profesor que estaba permitido.
2. **Ollama local en lugar de una API de pago**: el proyecto funciona sin internet y sin claves de API.
3. **Fragmentos de 700 palabras con 100 de solapamiento**: tamaño suficiente para dar contexto al modelo; el solapamiento evita cortar una idea por la mitad.
4. **Embedding guardado como texto con `stringtype=unspecified`**: PostgreSQL lo convierte a `vector(768)` sin librerías extra.
5. **Índice HNSW con distancia coseno**: búsqueda rápida de los fragmentos más parecidos.
6. **Copia del nombre del documento en `FuenteRespuesta`**: si se borra un PDF, el historial de respuestas no pierde la referencia.
7. **`CHECK` en la base de datos** para los 4 estados de consulta y para la calificación de 1 a 5: la regla se cumple aunque haya un error en el código.
8. **Relaciones excluidas de `equals`/`hashCode`** en las entidades con Lombok `@Data`, para evitar recursión infinita entre entidades relacionadas.

## 4. Cómo verifiqué lo que generó la IA

- Compilé el proyecto con `mvn compile` después de cada cambio.
- [Probé los endpoints con la colección de Postman: completar resultados]
- [Revisé con consultas SQL que los datos guardados coinciden con lo que muestra la app]
- Puedo explicar cada componente durante la exposición.
