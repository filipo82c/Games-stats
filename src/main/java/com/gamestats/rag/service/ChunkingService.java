package com.gamestats.rag.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Divide el texto del PDF en FRAGMENTOS (chunks) con solapamiento.
 *
 * Por que? Los modelos tienen limite de texto y la busqueda es mas precisa con
 * pedazos pequenos. El solapamiento evita cortar una idea a la mitad.
 *
 * Ejemplo con chunk-size=700 y overlap=100:
 *   Fragmento 1 = palabras 0..699
 *   Fragmento 2 = palabras 600..1299   (comparte 100 palabras con el anterior)
 */
@Service
public class ChunkingService {

    @Value("${app.chunking.chunk-size}")
    private int chunkSize;

    @Value("${app.chunking.chunk-overlap}")
    private int chunkOverlap;

    /** Un fragmento de texto y la pagina donde empieza. */
    public record Chunk(String content, int pageNumber) {}

    /**
     * @param pages texto de cada pagina del PDF (posicion 0 = pagina 1)
     */
    public List<Chunk> split(List<String> pages) {
        // 1. Juntar todas las palabras recordando de que pagina viene cada una
        List<String> words = new ArrayList<>();
        List<Integer> wordPages = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            for (String w : pages.get(i).split("\\s+")) {
                if (!w.isBlank()) {
                    words.add(w);
                    wordPages.add(i + 1);
                }
            }
        }

        // 2. Recorrer las palabras en ventanas de chunkSize, avanzando (chunkSize - overlap)
        List<Chunk> chunks = new ArrayList<>();
        int step = Math.max(1, chunkSize - chunkOverlap);
        for (int start = 0; start < words.size(); start += step) {
            int end = Math.min(start + chunkSize, words.size());
            String content = String.join(" ", words.subList(start, end));
            chunks.add(new Chunk(content, wordPages.get(start)));
            if (end == words.size()) break;
        }
        return chunks;
    }
}
