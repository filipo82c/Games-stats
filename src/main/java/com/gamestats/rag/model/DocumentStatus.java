package com.gamestats.rag.model;

/**
 * Estados posibles de un documento PDF en el sistema
 */
public enum DocumentStatus {
    PROCESSING,  // El PDF fue recibido y se esta procesando en segundo plano
    READY,       // La indexacion termino, el documento esta listo para consultas
    ERROR        // Ocurrio un error durante el procesamiento
}
