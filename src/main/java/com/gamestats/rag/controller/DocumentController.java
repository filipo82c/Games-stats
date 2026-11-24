package com.gamestats.rag.controller;

import com.gamestats.rag.dto.DocumentResponse;
import com.gamestats.rag.model.User;
import com.gamestats.rag.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * Endpoints de documentos (requieren JWT).
 *  POST   /api/documents          subir PDF (multipart)
 *  GET    /api/documents          listar mis documentos
 *  DELETE /api/documents/{id}     eliminar un documento
 */
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<DocumentResponse> upload(@RequestParam("file") MultipartFile file,
                                                   @RequestParam(value = "name", required = false) String name,
                                                   @RequestParam(value = "gameName", required = false) String gameName,
                                                   @RequestParam(value = "juegoId", required = false) Long juegoId,
                                                   @AuthenticationPrincipal User user) throws IOException {
        // 202 ACCEPTED: recibido, se procesa en segundo plano
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(documentService.upload(file, name, gameName, juegoId, user));
    }

    @GetMapping
    public List<DocumentResponse> list(@AuthenticationPrincipal User user) {
        return documentService.list(user);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        documentService.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
