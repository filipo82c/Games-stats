package com.gamestats.rag.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Envia el prompt al modelo de lenguaje (LLM) que corre localmente en Ollama
 * y devuelve la respuesta generada.
 */
@Service
@RequiredArgsConstructor
public class OllamaLlmService {

    private final RestClient ollamaRestClient;

    @Value("${ollama.llm-model}")
    private String llmModel;

    public String generate(String prompt) {
        Map<?, ?> response = ollamaRestClient.post()
                .uri("/api/generate")
                .body(Map.of(
                        "model", llmModel,
                        "prompt", prompt,
                        "stream", false,
                        // Temperatura baja = respuestas mas fieles al contexto, menos "inventadas"
                        "options", Map.of("temperature", 0.2)))
                .retrieve()
                .body(Map.class);

        if (response == null || response.get("response") == null) {
            throw new IllegalStateException("Ollama no devolvio respuesta. Esta corriendo el modelo " + llmModel + "?");
        }
        return response.get("response").toString().trim();
    }
}
