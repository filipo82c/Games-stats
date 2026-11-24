package com.gamestats.rag.model;

/**
 * Estados posibles de una consulta a la IA (los pide el enunciado)
 */
public enum EstadoConsulta {
    PENDIENTE,       // La pregunta se guardo y se esta procesando
    RESPONDIDA,      // La IA respondio usando fragmentos de los documentos
    SIN_RESULTADOS,  // No habia fragmentos relevantes: la IA no inventa
    ERROR            // Fallo algo (por ejemplo, Ollama no respondio)
}
