package com.exampbr.com.felipe.ecommerce_mercearia.controllers;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.AuthenticationDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.LoginResponseDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.RegisterDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Usuario;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.UsuarioRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.TentativaLoginRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.services.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints de autenticação")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TentativaLoginRepository tentativaLoginRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário")
    public ResponseEntity<LoginResponseDTO> registrar(@Valid @RequestBody RegisterDTO dto) {
        // Verificar se usuário já existe
        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(dto.email());

        if (usuarioExistente.isPresent()) {
            return ResponseEntity.badRequest().build();
        }

        // Criar novo usuário
        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.password()));

        usuarioRepository.save(usuario);

        // Gerar token
        String token = tokenService.generateToken(usuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new LoginResponseDTO(token));
    }

    @PostMapping("/login")
    @Operation(summary = "Fazer login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationDTO dto) {
        Optional<Usuario> usuario = usuarioRepository.findByEmail(dto.email());

        if (usuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Verificar senha
        if (!passwordEncoder.matches(dto.senha(), usuario.get().getSenha())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Gerar token
        String token = tokenService.generateToken(usuario.get());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token JWT")
    public ResponseEntity<LoginResponseDTO> refresh(@RequestHeader("Authorization") String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.badRequest().build();
        }

        String jwt = token.replace("Bearer ", "");

        try {
            String email = tokenService.validateToken(jwt);
            Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);

            if (usuarioOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String novoToken = tokenService.generateToken(usuarioOpt.get());
            return ResponseEntity.ok(new LoginResponseDTO(novoToken));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}