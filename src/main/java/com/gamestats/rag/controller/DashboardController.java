package com.gamestats.rag.controller;

import com.gamestats.rag.dto.DashboardResponse;
import com.gamestats.rag.model.User;
import com.gamestats.rag.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * Dashboard analitico (requiere JWT).
 *  GET /api/dashboard?desde=2026-11-01&hasta=2026-11-30&juegoId=1
 *  Todos los filtros son opcionales. Sin fechas: ultimos 30 dias.
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponse obtener(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long juegoId,
            @AuthenticationPrincipal User user) {
        return dashboardService.calcular(user, desde, hasta, juegoId);
    }
}
