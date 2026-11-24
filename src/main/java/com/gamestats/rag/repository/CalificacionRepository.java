package com.gamestats.rag.repository;

import com.gamestats.rag.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de calificaciones de respuestas.
 */
public interface CalificacionRepository extends JpaRepository<Calificacion, Long> {

    Optional<Calificacion> findByRespuestaId(Long respuestaId);
}
