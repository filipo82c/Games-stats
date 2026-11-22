package com.gamestats.rag.controller;

import com.gamestats.rag.dto.QueryRequest;
import com.gamestats.rag.dto.QueryResponse;
import com.gamestats.rag.model.User;
import com.gamestats.rag.service.RagQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoint de preguntas (requiere JWT).
 *  POST /api/query   { "question": "..." }
 */
@RestController
@RequestMapping("/api/query")
@RequiredArgsConstructor
public class QueryController {

    private final RagQueryService ragQueryService;

    @PostMapping
    public QueryResponse ask(@Valid @RequestBody QueryRequest request, @AuthenticationPrincipal User user) {
        return ragQueryService.ask(request.question(), user);
    }
}
