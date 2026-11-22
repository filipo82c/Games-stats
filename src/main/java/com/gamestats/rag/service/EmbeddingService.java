package com.gamestats.rag.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Convierte texto en un EMBEDDING (vector de 768 numeros) usando Ollama.
 *
 * Idea clave: textos con significado parecido producen vectores cercanos.
 * Asi "dano del rifle" y "damage del arma" quedan cerca aunque usen palabras distintas.
 */
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final RestClient ollamaRestClient;

    @Value("${ollama.embedding-model}")
    private String embeddingModel;

    /** Devuelve el vector en formato pgvector: "[0.12,-0.03,...]" */
    public String embed(String text) {
        Map<?, ?> response = ollamaRestClient.post()
                .uri("/api/embeddings")
                .body(Map.of("model", embeddingModel, "prompt", text))
                .retrieve()
                .body(Map.class);

        if (response == null || !(response.get("embedding") instanceof List<?> vector) || vector.isEmpty()) {
            throw new IllegalStateException("Ollama no devolvio un embedding. Esta corriendo el modelo " + embeddingModel + "?");
        }
        return vector.stream().map(String::valueOf).collect(Collectors.joining(",", "[", "]"));
    }

    public String getModelName() {
        return embeddingModel;
    }
}
