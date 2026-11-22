package com.gamestats.rag.service;

import com.gamestats.rag.dto.AuthRequest;
import com.gamestats.rag.dto.AuthResponse;
import com.gamestats.rag.dto.RegisterRequest;
import com.gamestats.rag.model.User;
import com.gamestats.rag.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Registro e inicio de sesion de usuarios.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalArgumentException("El nombre de usuario ya existe");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new IllegalArgumentException("El email ya esta registrado");
        }
        User user = User.builder()
                .username(req.username())
                .email(req.email())
                .password(passwordEncoder.encode(req.password())) // Nunca en texto plano
                .role("USER")
                .build();
        userRepository.save(user);
        return new AuthResponse(jwtService.generateToken(user), user.getUsername(), user.getRole());
    }

    public AuthResponse login(AuthRequest req) {
        // Lanza BadCredentialsException si el usuario o la contrasena son incorrectos
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password()));
        User user = userRepository.findByUsername(req.username()).orElseThrow();
        return new AuthResponse(jwtService.generateToken(user), user.getUsername(), user.getRole());
    }
}
