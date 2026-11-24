package com.gamestats.rag.repository;

import com.gamestats.rag.model.RespuestaIA;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de respuestas de la IA.
 */
public interface RespuestaIARepository extends JpaRepository<RespuestaIA, Long> {

    Optional<RespuestaIA> findByConsultaId(Long consultaId);
}
