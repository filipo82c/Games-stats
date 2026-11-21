package com.gamestats.rag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * GameStats AI — Sistema RAG para Videojuegos
 *
 * RAG = Retrieval-Augmented Generation (Generacion Aumentada por Recuperacion)
 *
 * Flujo del sistema:
 * 1. El usuario sube un PDF de un videojuego (patch notes, guia, lore, estadisticas)
 * 2. El sistema extrae el texto, lo divide en fragmentos y genera embeddings con Ollama
 * 3. Los embeddings se guardan en PostgreSQL con la extension pgvector
 * 4. Cuando el usuario hace una pregunta, se buscan los fragmentos mas relevantes
 * 5. Esos fragmentos + la pregunta se envian al LLM para generar una respuesta con citas
 */
@SpringBootApplication
@EnableAsync  // Habilita procesamiento asincrono para indexar PDFs sin bloquear al usuario
public class RagApplication {
    public static void main(String[] args) {
        SpringApplication.run(RagApplication.class, args);
        System.out.println("");
        System.out.println("╔══════════════════════════════════════════════╗");
        System.out.println("║      GameStats AI - RAG para Videojuegos     ║");
        System.out.println("║      http://localhost:8080                   ║");
        System.out.println("╚══════════════════════════════════════════════╝");
    }
}
