package com.gamestats.rag.repository;

import com.gamestats.rag.model.DocumentChunk;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repositorio de fragmentos de documentos.
 * Contiene la consulta SQL mas importante del sistema: la busqueda vectorial.
 */
public interface DocumentChunkRepository extends JpaRepository<DocumentChunk, Long> {

    /**
     * BUSQUEDA SEMANTICA: Encuentra los fragmentos mas similares al embedding de la pregunta.
     *
     * El operador <=> de pgvector calcula la distancia coseno entre dos vectores.
     * Vectores con distancia coseno menor = textos con significado mas similar.
     *
     * El filtro user_id = :userId garantiza que cada usuario solo ve sus propios documentos
     * (seguridad multi-tenant: varios usuarios pueden usar el sistema sin ver datos ajenos)
     *
     * @param userId          ID del usuario (filtro de seguridad)
     * @param queryEmbedding  Embedding de la pregunta en formato "[0.1, 0.2, ...]"
     * @param limit           Cuantos fragmentos devolver (normalmente 5)
     * @return Lista de fragmentos ordenados por similitud (el mas relevante primero)
     */
    @Query(value = """
            SELECT * FROM document_chunks
            WHERE user_id = :userId
            ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<DocumentChunk> findSimilarChunks(
            @Param("userId") Long userId,
            @Param("queryEmbedding") String queryEmbedding,
            @Param("limit") int limit
    );

    /**
     * Igual que findSimilarChunks, pero devuelve pares [chunk_id, similitud_coseno].
     * En pgvector <=> es distancia coseno (0 = identico), asi que similitud = 1 - distancia.
     */
    @Query(value = """
            SELECT id, (1 - (embedding <=> CAST(:queryEmbedding AS vector))) AS similitud
            FROM document_chunks
            WHERE user_id = :userId
            ORDER BY embedding <=> CAST(:queryEmbedding AS vector)
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findSimilarChunkIdsWithSimilarity(
            @Param("userId") Long userId,
            @Param("queryEmbedding") String queryEmbedding,
            @Param("limit") int limit
    );

    // Carga los fragmentos junto con su documento en una sola consulta (evita LazyInitializationException)
    @Query("SELECT c FROM DocumentChunk c JOIN FETCH c.document WHERE c.id IN :ids")
    List<DocumentChunk> findAllByIdWithDocument(@Param("ids") List<Long> ids);

    // Contar los fragmentos de un documento (para estadisticas)
    long countByDocumentId(Long documentId);

    // Eliminar todos los fragmentos de un documento (cascade lo hace automaticamente)
    void deleteByDocumentId(Long documentId);
}
