package com.gamestats.rag.controller;

import com.gamestats.rag.dto.JuegoRequest;
import com.gamestats.rag.dto.JuegoResponse;
import com.gamestats.rag.service.JuegoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints CRUD de videojuegos (requieren JWT).
 *  GET    /api/juegos        listar todos los juegos
 *  GET    /api/juegos/{id}   obtener un juego
 *  POST   /api/juegos        crear un juego
 *  PUT    /api/juegos/{id}   actualizar un juego
 *  DELETE /api/juegos/{id}   eliminar un juego
 */
@RestController
@RequestMapping("/api/juegos")
@RequiredArgsConstructor
public class JuegoController {

    private final JuegoService juegoService;

    @GetMapping
    public List<JuegoResponse> listar() {
        return juegoService.listar();
    }

    @GetMapping("/{id}")
    public JuegoResponse obtenerPorId(@PathVariable Long id) {
        return juegoService.obtenerPorId(id);
    }

    @PostMapping
    public ResponseEntity<JuegoResponse> crear(@Valid @RequestBody JuegoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(juegoService.crear(request));
    }

    @PutMapping("/{id}")
    public JuegoResponse actualizar(@PathVariable Long id, @Valid @RequestBody JuegoRequest request) {
        return juegoService.actualizar(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        juegoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
