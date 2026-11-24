package com.gamestats.rag.repository;

import com.gamestats.rag.model.ConsultaIA;
import com.gamestats.rag.model.EstadoConsulta;
import com.gamestats.rag.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Repositorio de consultas a la IA (historial).
 */
public interface ConsultaIARepository extends JpaRepository<ConsultaIA, Long> {

    // Historial del usuario, la mas reciente primero
    List<ConsultaIA> findByUserOrderByCreatedAtDesc(User user);

    List<ConsultaIA> findByUserAndEstado(User user, EstadoConsulta estado);

    List<ConsultaIA> findByUserAndEstadoOrderByCreatedAtDesc(User user, EstadoConsulta estado);
}
