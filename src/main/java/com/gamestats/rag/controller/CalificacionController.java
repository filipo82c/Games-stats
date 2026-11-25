package com.gamestats.rag.controller;

import com.gamestats.rag.dto.CalificacionRequest;
import com.gamestats.rag.dto.CalificacionResponse;
import com.gamestats.rag.model.User;
import com.gamestats.rag.service.CalificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de calificaciones de respuestas (requieren JWT).
 *  POST   /api/respuestas/{respuestaId}/calificacion   calificar una respuesta (1 a 5)
 *  GET    /api/respuestas/{respuestaId}/calificacion   obtener la calificacion
 *  PUT    /api/respuestas/{respuestaId}/calificacion   actualizar la calificacion
 *  DELETE /api/respuestas/{respuestaId}/calificacion   eliminar la calificacion
 */
@RestController
@RequestMapping("/api/respuestas/{respuestaId}/calificacion")
@RequiredArgsConstructor
public class CalificacionController {

    private final CalificacionService calificacionService;

    @PostMapping
    public ResponseEntity<CalificacionResponse> calificar(@PathVariable Long respuestaId,
                                                          @Valid @RequestBody CalificacionRequest request,
                                                          @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(calificacionService.calificar(respuestaId, request, user));
    }

    @GetMapping
    public CalificacionResponse obtener(@PathVariable Long respuestaId,
                                        @AuthenticationPrincipal User user) {
        return calificacionService.obtener(respuestaId, user);
    }

    @PutMapping
    public CalificacionResponse actualizar(@PathVariable Long respuestaId,
                                           @Valid @RequestBody CalificacionRequest request,
                                           @AuthenticationPrincipal User user) {
        return calificacionService.actualizar(respuestaId, request, user);
    }

    @DeleteMapping
    public ResponseEntity<Void> eliminar(@PathVariable Long respuestaId,
                                         @AuthenticationPrincipal User user) {
        calificacionService.eliminar(respuestaId, user);
        return ResponseEntity.noContent().build();
    }
}
