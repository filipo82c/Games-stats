package com.gamestats.rag.controller;

import com.gamestats.rag.dto.ConsultaHistorialResponse;
import com.gamestats.rag.model.EstadoConsulta;
import com.gamestats.rag.model.User;
import com.gamestats.rag.service.HistorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints del historial de consultas del usuario (requieren JWT).
 *  GET    /api/consultas                  listar mis consultas (mas reciente primero, opcional ?estado=RESPONDIDA)
 *  GET    /api/consultas/{id}             ver detalle de una consulta con respuesta y fuentes con similitud
 *  DELETE /api/consultas/{id}             eliminar una consulta del historial
 */
@RestController
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
public class HistorialController {

    private final HistorialService historialService;

    @GetMapping
    public List<ConsultaHistorialResponse> listar(
            @RequestParam(value = "estado", required = false) EstadoConsulta estado,
            @AuthenticationPrincipal User user) {
        return historialService.listar(user, estado);
    }

    @GetMapping("/{id}")
    public ConsultaHistorialResponse obtenerPorId(@PathVariable Long id,
                                                  @AuthenticationPrincipal User user) {
        return historialService.obtenerPorId(id, user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @AuthenticationPrincipal User user) {
        historialService.eliminar(id, user);
        return ResponseEntity.noContent().build();
    }
}
