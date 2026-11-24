package com.gamestats.rag.service;

import com.gamestats.rag.dto.JuegoRequest;
import com.gamestats.rag.dto.JuegoResponse;
import com.gamestats.rag.model.Juego;
import com.gamestats.rag.repository.JuegoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Logica de negocio para el catalogo de videojuegos (categorias de documentos).
 */
@Service
@RequiredArgsConstructor
public class JuegoService {

    private final JuegoRepository juegoRepository;

    @Transactional(readOnly = true)
    public List<JuegoResponse> listar() {
        return juegoRepository.findAllByOrderByNombreAsc().stream()
                .map(JuegoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public JuegoResponse obtenerPorId(Long id) {
        Juego juego = juegoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));
        return JuegoResponse.from(juego);
    }

    @Transactional
    public JuegoResponse crear(JuegoRequest request) {
        String nombre = request.nombre().trim();
        if (juegoRepository.existsByNombreIgnoreCase(nombre)) {
            throw new IllegalArgumentException("Ya existe un juego con el nombre '" + nombre + "'");
        }
        Juego juego = juegoRepository.save(Juego.builder()
                .nombre(nombre)
                .genero(limpiarOpcional(request.genero()))
                .descripcion(limpiarOpcional(request.descripcion()))
                .build());
        return JuegoResponse.from(juego);
    }

    @Transactional
    public JuegoResponse actualizar(Long id, JuegoRequest request) {
        Juego juego = juegoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));

        String nuevoNombre = request.nombre().trim();
        if (!juego.getNombre().equalsIgnoreCase(nuevoNombre)
                && juegoRepository.existsByNombreIgnoreCase(nuevoNombre)) {
            throw new IllegalArgumentException("Ya existe un juego con el nombre '" + nuevoNombre + "'");
        }

        juego.setNombre(nuevoNombre);
        juego.setGenero(limpiarOpcional(request.genero()));
        juego.setDescripcion(limpiarOpcional(request.descripcion()));
        return JuegoResponse.from(juegoRepository.save(juego));
    }

    @Transactional
    public void eliminar(Long id) {
        Juego juego = juegoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));
        juegoRepository.delete(juego);
    }

    private String limpiarOpcional(String valor) {
        return (valor == null || valor.isBlank()) ? null : valor.trim();
    }
}
