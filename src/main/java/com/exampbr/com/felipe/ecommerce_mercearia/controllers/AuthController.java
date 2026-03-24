package com.exampbr.com.felipe.ecommerce_mercearia.controllers;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.AuthenticationDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.ErroLoginDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.LoginResponseDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.RegistroDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.models.TentativaLogin;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Usuario;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.TentativaLoginRepository;
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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints de autenticação de usuários")
@RequiredArgsConstructor
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final TokenService tokenService;
    private final TentativaLoginRepository tentativaLoginRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Cria uma nova conta de usuário")
    public ResponseEntity<?> register(@Valid @RequestBody RegistroDTO dto) {
        try {
            // Verificar se email já existe
            var usuarioExistente = usuarioRepository.findByEmail(dto.email());
            if (usuarioExistente != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(new ErroLoginDTO("Email já registrado", "Este email já está em uso"));
            }

            // Criar novo usuário
            var usuario = new Usuario();
            usuario.setNome(dto.nome());
            usuario.setEmail(dto.email());
            usuario.setSenha(new BCryptPasswordEncoder().encode(dto.password()));
            usuario.setRole("USER");
            usuario.setCreatedAt(OffsetDateTime.now());
            usuario.setUpdatedAt(OffsetDateTime.now());

            usuarioRepository.save(usuario);

            var token = tokenService.gerarAccessToken(usuario);
            return ResponseEntity.ok(new LoginResponseDTO(token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErroLoginDTO("Erro ao registrar", e.getMessage()));
        }
    }

    @PostMapping("/login")
    @Operation(summary = "Login do usuário", description = "Realiza login do usuário e retorna JWT token")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationDTO dto) {
        try {
            // Buscar usuário por email
            var usuario = usuarioRepository.findByEmail(dto.email());

            if (usuario == null) {
                // Registrar tentativa de login falha
                registrarTentativaFalha(dto.email());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErroLoginDTO("Credenciais inválidas", "Email ou senha incorretos"));
            }

            // Verificar se conta está bloqueada por muitas tentativas
            var tentativaOpt = tentativaLoginRepository.findByEmail(dto.email());
            if (tentativaOpt.isPresent() && tentativaOpt.get().getBloqueado()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new ErroLoginDTO("Conta bloqueada", "Muitas tentativas de login. Tente mais tarde."));
            }

            // Verificar senha (AuthenticationDTO usa 'senha', não 'password')
            if (!new BCryptPasswordEncoder().matches(dto.senha(), usuario.getSenha())) {
                registrarTentativaFalha(dto.email());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErroLoginDTO("Credenciais inválidas", "Email ou senha incorretos"));
            }

            // Login bem-sucedido - limpar tentativas
            if (tentativaOpt.isPresent()) {
                var t = tentativaOpt.get();
                t.setTentativas(0);
                t.setBloqueado(false);
                tentativaLoginRepository.save(t);
            }

            // Gerar token JWT
            var token = tokenService.gerarAccessToken(usuario);
            return ResponseEntity.ok(new LoginResponseDTO(token));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErroLoginDTO("Erro ao fazer login", e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar token JWT", description = "Gera um novo token JWT usando um token válido")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErroLoginDTO("Token inválido", "Authorization header ausente ou malformado"));
            }

            String token = authHeader.substring(7);
            String email = tokenService.validarRefreshToken(token);

            if (email == null || email.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErroLoginDTO("Token expirado", "Token inválido ou expirado"));
            }

            var usuario = usuarioRepository.findByEmail(email);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErroLoginDTO("Usuário não encontrado", "Usuário associado ao token não existe"));
            }

            var novoToken = tokenService.gerarAccessToken(usuario);
            return ResponseEntity.ok(new LoginResponseDTO(novoToken));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErroLoginDTO("Erro ao renovar token", e.getMessage()));
        }
    }

    /**
     * Registra uma tentativa de login falha
     */
    private void registrarTentativaFalha(String email) {
        var tentativaOpt = tentativaLoginRepository.findByEmail(email);

        if (tentativaOpt.isPresent()) {
            var tentativa = tentativaOpt.get();
            tentativa.setTentativas(tentativa.getTentativas() + 1);
            tentativa.setUltimaTentativa(LocalDateTime.now());

            // Bloquear após 5 tentativas
            if (tentativa.getTentativas() >= 5) {
                tentativa.setBloqueado(true);
                tentativa.setDataBloqueio(LocalDateTime.now());
            }

            tentativaLoginRepository.save(tentativa);
        } else {
            // Criar novo registro de tentativa
            var novaT = new TentativaLogin();
            novaT.setEmail(email);
            novaT.setTentativas(1);
            novaT.setUltimaTentativa(LocalDateTime.now());
            novaT.setBloqueado(false);
            tentativaLoginRepository.save(novaT);
        }
    }
}