package com.gamestats.rag.service;

import com.gamestats.rag.dto.DocumentResponse;
import com.gamestats.rag.model.Document;
import com.gamestats.rag.model.DocumentStatus;
import com.gamestats.rag.model.Juego;
import com.gamestats.rag.model.User;
import com.gamestats.rag.repository.DocumentRepository;
import com.gamestats.rag.repository.JuegoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

/**
 * Gestion de documentos: subir, listar y eliminar.
 * La subida responde de inmediato y la indexacion sigue en segundo plano.
 */
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final JuegoRepository juegoRepository;
    private final PdfProcessorService pdfProcessorService;

    @Value("${app.upload.temp-dir}")
    private String tempDir;

    @Value("${app.upload.max-file-size}")
    private long maxFileSize;

    public DocumentResponse upload(MultipartFile file, String name, String gameName, Long juegoId, User user) throws IOException {
        // Validaciones
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Debes seleccionar un archivo");
        if (file.getSize() > maxFileSize) throw new IllegalArgumentException("El archivo supera los 20MB");
        String filename = file.getOriginalFilename() == null ? "documento.pdf" : file.getOriginalFilename();
        if (!filename.toLowerCase().endsWith(".pdf")) throw new IllegalArgumentException("Solo se permiten archivos PDF");

        // Resolver juego opcional (por ID o por nombre si ya existe)
        Juego juego = null;
        String resolvedGameName = (gameName == null || gameName.isBlank()) ? null : gameName.trim();
        if (juegoId != null) {
            juego = juegoRepository.findById(juegoId)
                    .orElseThrow(() -> new IllegalArgumentException("Juego no encontrado"));
            if (resolvedGameName == null) {
                resolvedGameName = juego.getNombre();
            }
        } else if (resolvedGameName != null) {
            juego = juegoRepository.findByNombreIgnoreCase(resolvedGameName).orElse(null);
        }

        // Guardar temporalmente y calcular hash SHA-256
        Path dir = Path.of(tempDir);
        Files.createDirectories(dir);
        Path temp = dir.resolve(UUID.randomUUID() + ".pdf");
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, temp, StandardCopyOption.REPLACE_EXISTING);
        }

        Document doc = documentRepository.save(Document.builder()
                .user(user)
                .name(name == null || name.isBlank() ? filename : name)
                .originalFilename(filename)
                .gameName(resolvedGameName)
                .juego(juego)
                .fileHash(sha256(temp))
                .status(DocumentStatus.PROCESSING)
                .build());

        // Lanzar indexacion en segundo plano (no bloquea la respuesta)
        pdfProcessorService.processDocument(doc.getId(), user.getId(), temp);
        return DocumentResponse.from(doc);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> list(User user) {
        return documentRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(DocumentResponse::from)
                .toList();
    }

    @org.springframework.transaction.annotation.Transactional
    public void delete(Long id, User user) {
        Document doc = documentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Documento no encontrado"));
        if (!doc.getUser().getId().equals(user.getId())) {
            throw new SecurityException("No tienes permiso sobre este documento");
        }
        documentRepository.delete(doc); // ON DELETE CASCADE borra tambien sus fragmentos
    }

    private String sha256(Path file) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(Files.readAllBytes(file)));
        } catch (Exception e) {
            throw new IOException("No se pudo calcular el hash", e);
        }
    }
}
