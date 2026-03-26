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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Endpoints para autenticação e autorização")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Cria um novo usuário no sistema")
    public ResponseEntity<LoginResponseDTO> register(@Valid @RequestBody RegisterDTO dto) {
        if (usuarioRepository.findByEmail(dto.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        Usuario usuario = new Usuario();
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        usuario.setSenha(passwordEncoder.encode(dto.password()));

        usuarioRepository.save(usuario);

        String token = tokenService.generateToken(usuario);
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/login")
    @Operation(summary = "Fazer login", description = "Autentica usuário e retorna JWT")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody AuthenticationDTO dto) {
        var usuario = usuarioRepository.findByEmail(dto.email())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        if (!passwordEncoder.matches(dto.senha(), usuario.getSenha())) {
            throw new RuntimeException("Senha inválida");
        }

        String token = tokenService.generateToken(usuario);
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token", description = "Renova o JWT usando um refresh token")
    public ResponseEntity<LoginResponseDTO> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        String email = tokenService.validateToken(token);

        var usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        String novoToken = tokenService.generateToken(usuario);
        return ResponseEntity.ok(new LoginResponseDTO(novoToken));
    }
}