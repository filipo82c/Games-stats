package com.gamestats.rag.dto;

import java.util.List;

/**
 * Respuesta del sistema RAG: el texto generado por la IA + las fuentes (citas)
 * de donde saco la informacion.
 */
public record QueryResponse(
        String question,
        String answer,
        List<Source> sources,
        long responseTimeMs
) {
    /** Una cita: de que documento y pagina viene el fragmento usado. */
    public record Source(
            int number,
            Long documentId,
            String documentName,
            String gameName,
            Integer pageNumber,
            String excerpt
    ) {}
}
