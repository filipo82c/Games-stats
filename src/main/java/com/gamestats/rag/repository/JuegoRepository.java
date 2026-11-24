package com.gamestats.rag.repository;

import com.gamestats.rag.model.Juego;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de juegos.
 */
public interface JuegoRepository extends JpaRepository<Juego, Long> {

    List<Juego> findAllByOrderByNombreAsc();

    Optional<Juego> findByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}
