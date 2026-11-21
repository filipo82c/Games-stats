package com.gamestats.rag.repository;

import com.gamestats.rag.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repositorio de usuarios.
 * Spring Data JPA genera automaticamente las implementaciones de estos metodos.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    // Buscar usuario por nombre de usuario (para login)
    Optional<User> findByUsername(String username);

    // Buscar usuario por email
    Optional<User> findByEmail(String email);

    // Verificar si ya existe un username (para registro)
    boolean existsByUsername(String username);

    // Verificar si ya existe un email (para registro)
    boolean existsByEmail(String email);
}
