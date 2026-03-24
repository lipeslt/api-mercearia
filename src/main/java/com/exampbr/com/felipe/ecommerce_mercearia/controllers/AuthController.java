package com.exampbr.com.felipe.ecommerce_mercearia.controllers;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.AuthenticationDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.LoginResponseDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.RegisterDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Usuario;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.UsuarioRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.services.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints de autenticação")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterDTO dto) {
        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(dto.email());
        if (usuarioExistente.isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("{\"erro\": \"Email já cadastrado\"}");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(bCryptPasswordEncoder.encode(dto.password()));
        usuario.setRole("USER");

        usuarioRepository.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("{\"mensagem\": \"Usuário registrado com sucesso\"}");
    }

    @PostMapping("/login")
    @Operation(summary = "Login do usuário")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationDTO dto) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(dto.email());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"erro\": \"Usuário não encontrado\"}");
        }

        Usuario usuario = usuarioOpt.get();

        if (!bCryptPasswordEncoder.matches(dto.senha(), usuario.getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"erro\": \"Senha incorreta\"}");
        }

        String token = tokenService.generateToken(usuario);
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token JWT")
    public ResponseEntity<?> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"erro\": \"Token não fornecido\"}");
        }

        String token = authHeader.substring(7);
        String email = tokenService.validateToken(token);

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"erro\": \"Token inválido\"}");
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("{\"erro\": \"Usuário não encontrado\"}");
        }

        String novoToken = tokenService.generateToken(usuarioOpt.get());
        return ResponseEntity.ok(new LoginResponseDTO(novoToken));
    }
}