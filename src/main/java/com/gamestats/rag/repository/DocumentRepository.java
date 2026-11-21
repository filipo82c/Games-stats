package com.gamestats.rag.repository;

import com.gamestats.rag.model.Document;
import com.gamestats.rag.model.DocumentStatus;
import com.gamestats.rag.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio de documentos.
 */
public interface DocumentRepository extends JpaRepository<Document, Long> {

    // Listar todos los documentos de un usuario, ordenados por fecha (mas reciente primero)
    List<Document> findByUserOrderByCreatedAtDesc(User user);

    // Listar documentos de un usuario filtrados por estado
    List<Document> findByUserAndStatus(User user, DocumentStatus status);
}
