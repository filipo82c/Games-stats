package com.gamestats.rag.service;

import com.gamestats.rag.dto.CalificacionRequest;
import com.gamestats.rag.dto.CalificacionResponse;
import com.gamestats.rag.model.Calificacion;
import com.gamestats.rag.model.RespuestaIA;
import com.gamestats.rag.model.User;
import com.gamestats.rag.repository.CalificacionRepository;
import com.gamestats.rag.repository.RespuestaIARepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestion de calificaciones (1 a 5) sobre respuestas de la IA.
 * Valida que solo el propietario de la consulta pueda ver o calificar su respuesta.
 */
@Service
@RequiredArgsConstructor
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final RespuestaIARepository respuestaRepository;

    @Transactional
    public CalificacionResponse calificar(Long respuestaId, CalificacionRequest request, User user) {
        RespuestaIA respuesta = buscarRespuestaYValidarPropietario(respuestaId, user);
        if (calificacionRepository.findByRespuestaId(respuestaId).isPresent()) {
            throw new IllegalArgumentException("Esta respuesta ya fue calificada. Usa PUT para actualizarla.");
        }

        Calificacion calificacion = calificacionRepository.save(Calificacion.builder()
                .respuesta(respuesta)
                .puntuacion(request.puntuacion())
                .comentario(limpiarOpcional(request.comentario()))
                .build());
        return CalificacionResponse.from(calificacion);
    }

    @Transactional
    public CalificacionResponse actualizar(Long respuestaId, CalificacionRequest request, User user) {
        buscarRespuestaYValidarPropietario(respuestaId, user);
        Calificacion calificacion = calificacionRepository.findByRespuestaId(respuestaId)
                .orElseThrow(() -> new IllegalArgumentException("Esta respuesta aun no tiene calificacion"));

        calificacion.setPuntuacion(request.puntuacion());
        calificacion.setComentario(limpiarOpcional(request.comentario()));
        return CalificacionResponse.from(calificacionRepository.save(calificacion));
    }

    @Transactional(readOnly = true)
    public CalificacionResponse obtener(Long respuestaId, User user) {
        buscarRespuestaYValidarPropietario(respuestaId, user);
        Calificacion calificacion = calificacionRepository.findByRespuestaId(respuestaId)
                .orElseThrow(() -> new IllegalArgumentException("Esta respuesta aun no tiene calificacion"));
        return CalificacionResponse.from(calificacion);
    }

    @Transactional
    public void eliminar(Long respuestaId, User user) {
        buscarRespuestaYValidarPropietario(respuestaId, user);
        Calificacion calificacion = calificacionRepository.findByRespuestaId(respuestaId)
                .orElseThrow(() -> new IllegalArgumentException("Esta respuesta aun no tiene calificacion"));
        calificacionRepository.delete(calificacion);
    }

    private RespuestaIA buscarRespuestaYValidarPropietario(Long respuestaId, User user) {
        RespuestaIA respuesta = respuestaRepository.findById(respuestaId)
                .orElseThrow(() -> new IllegalArgumentException("Respuesta no encontrada"));
        if (!respuesta.getConsulta().getUser().getId().equals(user.getId())) {
            throw new SecurityException("No tienes permiso para calificar esta respuesta");
        }
        return respuesta;
    }

    private String limpiarOpcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
