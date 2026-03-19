package com.exampbr.com.felipe.ecommerce_mercearia.controllers;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.AuthenticationDTO;
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
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints para login e registro")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "Login de usuário", description = "Realiza login e retorna JWT token")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationDTO data) {
        var usuario = usuarioRepository.findByEmail(data.email());

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou senha inválidos");
        }

        if (!passwordEncoder.matches(data.senha(), usuario.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Email ou senha inválidos");
        }

        var token = tokenService.gerarToken(usuario);
        return ResponseEntity.ok(new TokenResponseDTO(token, "Bearer"));
    }

    @PostMapping("/registrar")
    @Operation(summary = "Registrar novo usuário", description = "Cria um novo usuário")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegisterDTO data) {
        if (usuarioRepository.findByEmail(data.email()) != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email já cadastrado");
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(data.nome());
        novoUsuario.setEmail(data.email());
        novoUsuario.setSenha(passwordEncoder.encode(data.senha()));
        novoUsuario.setAtivo(true);

        usuarioRepository.save(novoUsuario);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Usuário registrado com sucesso");
    }

    public static class TokenResponseDTO {
        public String token;
        public String type;

        public TokenResponseDTO(String token, String type) {
            this.token = token;
            this.type = type;
        }
    }
}