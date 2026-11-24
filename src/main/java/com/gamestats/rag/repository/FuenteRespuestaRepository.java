package com.gamestats.rag.repository;

import com.gamestats.rag.model.FuenteRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio de fuentes usadas en cada respuesta.
 */
public interface FuenteRespuestaRepository extends JpaRepository<FuenteRespuesta, Long> {

    List<FuenteRespuesta> findByRespuestaIdOrderByPosicionAsc(Long respuestaId);
}
