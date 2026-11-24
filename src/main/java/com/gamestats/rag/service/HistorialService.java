package com.gamestats.rag.service;

import com.gamestats.rag.dto.ConsultaHistorialResponse;
import com.gamestats.rag.model.ConsultaIA;
import com.gamestats.rag.model.EstadoConsulta;
import com.gamestats.rag.model.User;
import com.gamestats.rag.repository.ConsultaIARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion del historial de consultas RAG del usuario autenticado (multi-tenant).
 */
@Service
@RequiredArgsConstructor
public class HistorialService {

    private final ConsultaIARepository consultaRepository;

    @Transactional(readOnly = true)
    public List<ConsultaHistorialResponse> listar(User user, EstadoConsulta estado) {
        List<ConsultaIA> consultas = (estado == null)
                ? consultaRepository.findByUserOrderByCreatedAtDesc(user)
                : consultaRepository.findByUserAndEstadoOrderByCreatedAtDesc(user, estado);

        return consultas.stream()
                .map(ConsultaHistorialResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ConsultaHistorialResponse obtenerPorId(Long id, User user) {
        ConsultaIA consulta = buscarYValidarPropietario(id, user);
        return ConsultaHistorialResponse.from(consulta);
    }

    @Transactional
    public void eliminar(Long id, User user) {
        ConsultaIA consulta = buscarYValidarPropietario(id, user);
        // ON DELETE CASCADE elimina tambien la respuesta, sus fuentes y su calificacion
        consultaRepository.delete(consulta);
    }

    private ConsultaIA buscarYValidarPropietario(Long id, User user) {
        ConsultaIA consulta = consultaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Consulta no encontrada"));
        if (!consulta.getUser().getId().equals(user.getId())) {
            throw new SecurityException("No tienes permiso sobre esta consulta");
        }
        return consulta;
    }
}
