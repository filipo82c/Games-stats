package com.gamestats.rag.service;

import com.gamestats.rag.model.Document;
import com.gamestats.rag.model.DocumentChunk;
import com.gamestats.rag.model.DocumentStatus;
import com.gamestats.rag.repository.DocumentChunkRepository;
import com.gamestats.rag.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * PIPELINE DE INDEXACION (se ejecuta en segundo plano con @Async):
 *
 *   PDF -> extraer texto por pagina -> dividir en fragmentos
 *       -> generar embedding de cada fragmento -> guardar en pgvector
 *       -> borrar el PDF temporal
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PdfProcessorService {

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository chunkRepository;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;

    @Async
    public void processDocument(Long documentId, Long userId, Path tempFile) {
        Document document = documentRepository.findById(documentId).orElse(null);
        if (document == null) return;

        try {
            log.info("Indexando documento {} ({})", documentId, document.getOriginalFilename());

            // 1. Extraer el texto de cada pagina
            List<String> pages = extractPages(tempFile);
            if (pages.stream().allMatch(String::isBlank)) {
                throw new IllegalStateException("El PDF no contiene texto extraible (puede ser solo imagenes)");
            }

            // 2. Dividir en fragmentos
            List<ChunkingService.Chunk> chunks = chunkingService.split(pages);

            // 3. Generar embedding para cada fragmento y guardarlo
            for (int i = 0; i < chunks.size(); i++) {
                ChunkingService.Chunk c = chunks.get(i);
                chunkRepository.save(DocumentChunk.builder()
                        .document(document)
                        .userId(userId)
                        .content(c.content())
                        .embedding(embeddingService.embed(c.content()))
                        .pageNumber(c.pageNumber())
                        .chunkIndex(i)
                        .embeddingModel(embeddingService.getModelName())
                        .build());
            }

            // 4. Marcar como listo
            document.setTotalPages(pages.size());
            document.setTotalChunks(chunks.size());
            document.setStatus(DocumentStatus.READY);
            documentRepository.save(document);
            log.info("Documento {} listo: {} paginas, {} fragmentos", documentId, pages.size(), chunks.size());

        } catch (Exception e) {
            log.error("Error indexando documento {}", documentId, e);
            document.setStatus(DocumentStatus.ERROR);
            document.setErrorMessage(e.getMessage());
            documentRepository.save(document);
        } finally {
            // 5. El PDF NUNCA se guarda: se elimina siempre
            try {
                Files.deleteIfExists(tempFile);
            } catch (IOException ignored) {
            }
        }
    }

    private List<String> extractPages(Path file) throws IOException {
        List<String> pages = new ArrayList<>();
        try (PDDocument pdf = Loader.loadPDF(file.toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            for (int p = 1; p <= pdf.getNumberOfPages(); p++) {
                stripper.setStartPage(p);
                stripper.setEndPage(p);
                // Quitar caracteres nulos que PostgreSQL no acepta en TEXT
                pages.add(stripper.getText(pdf).replace("\u0000", ""));
            }
        }
        return pages;
    }
}
