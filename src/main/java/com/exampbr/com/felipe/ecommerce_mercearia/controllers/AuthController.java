package com.exampbr.com.felipe.ecommerce_mercearia.controllers;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.AuthenticationDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.RegisterDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.models.TentativaLogin;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Usuario;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.TentativaLoginRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.UsuarioRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.services.TokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticação", description = "Endpoints para login e registro")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final Integer MAX_TENTATIVAS = 5;
    private static final Integer TEMPO_BLOQUEIO_MINUTOS = 15;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TentativaLoginRepository tentativaLoginRepository;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    @Operation(summary = "Login de usuário", description = "Realiza login e retorna JWT tokens (Access Token e Refresh Token)")
    public ResponseEntity<?> login(@Valid @RequestBody AuthenticationDTO data) {
        logger.info("Tentativa de login para email: {}", data.email());

        // Verificar se a conta está bloqueada
        Optional<TentativaLogin> tentativaOpt = tentativaLoginRepository.findByEmail(data.email());
        if (tentativaOpt.isPresent()) {
            TentativaLogin tentativa = tentativaOpt.get();

            if (tentativa.getBloqueado()) {
                LocalDateTime dataDesbloqueio = tentativa.getDataBloqueio().plusMinutes(TEMPO_BLOQUEIO_MINUTOS);

                if (LocalDateTime.now().isBefore(dataDesbloqueio)) {
                    long minutosRestantes = java.time.temporal.ChronoUnit.MINUTES
                            .between(LocalDateTime.now(), dataDesbloqueio);
                    logger.warn("Tentativa de login com conta bloqueada: {}", data.email());
                    return ResponseEntity.status(HttpStatus.LOCKED)
                            .body(new ErroLoginDTO(
                                    "Conta bloqueada temporariamente",
                                    "Sua conta foi bloqueada após múltiplas tentativas de login incorretas. Tente novamente em " + minutosRestantes + " minutos."
                            ));
                } else {
                    // Desbloquear a conta
                    tentativa.setBloqueado(false);
                    tentativa.setTentativas(0);
                    tentativa.setDataBloqueio(null);
                    tentativaLoginRepository.save(tentativa);
                }
            }
        }

        // Validar usuário
        Usuario usuario = usuarioRepository.findByEmail(data.email());

        if (usuario == null) {
            registrarTentativaFalhada(data.email());
            logger.warn("Usuário não encontrado: {}", data.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErroLoginDTO(
                            "Email ou senha inválidos",
                            "Verifique suas credenciais e tente novamente"
                    ));
        }

        if (!passwordEncoder.matches(data.senha(), usuario.getPassword())) {
            registrarTentativaFalhada(data.email());
            logger.warn("Senha inválida para usuário: {}", data.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErroLoginDTO(
                            "Email ou senha inválidos",
                            "Verifique suas credenciais e tente novamente"
                    ));
        }

        // Login bem-sucedido - limpar tentativas
        tentativaLoginRepository.findByEmail(data.email()).ifPresent(tentativa -> {
            tentativa.setTentativas(0);
            tentativa.setBloqueado(false);
            tentativa.setDataBloqueio(null);
            tentativaLoginRepository.save(tentativa);
        });

        // Gerar tokens
        String accessToken = tokenService.gerarAccessToken(usuario);
        String refreshToken = tokenService.gerarRefreshToken(usuario);

        logger.info("Login bem-sucedido para usuário: {}", data.email());

        return ResponseEntity.ok(new LoginResponseDTO(
                accessToken,
                refreshToken,
                "Bearer",
                3600L // 1 hora em segundos
        ));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Renovar Access Token", description = "Usa o Refresh Token para gerar um novo Access Token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenDTO dto) {
        logger.info("Tentativa de renovar token");

        try {
            String email = tokenService.validarRefreshToken(dto.refreshToken());

            if (email == null || email.isEmpty()) {
                logger.warn("Refresh Token inválido ou expirado");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErroLoginDTO(
                                "Refresh Token inválido",
                                "Faça login novamente"
                        ));
            }

            Usuario usuario = usuarioRepository.findByEmail(email);
            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErroLoginDTO(
                                "Usuário não encontrado",
                                "Faça login novamente"
                        ));
            }

            String novoAccessToken = tokenService.gerarAccessToken(usuario);
            logger.info("Access Token renovado com sucesso para: {}", email);

            return ResponseEntity.ok(new LoginResponseDTO(
                    novoAccessToken,
                    dto.refreshToken(),
                    "Bearer",
                    3600L
            ));
        } catch (Exception e) {
            logger.error("Erro ao renovar token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErroLoginDTO(
                            "Erro ao renovar token",
                            "Faça login novamente"
                    ));
        }
    }

    @PostMapping("/registrar")
    @Operation(summary = "Registrar novo usuário", description = "Cria um novo usuário com email e senha")
    public ResponseEntity<?> registrar(@Valid @RequestBody RegisterDTO data) {
        logger.info("Tentativa de registro para email: {}", data.email());

        // Validar se usuário já existe
        if (usuarioRepository.findByEmail(data.email()) != null) {
            logger.warn("Email já cadastrado: {}", data.email());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErroLoginDTO(
                            "Email já cadastrado",
                            "Esse email já está registrado no sistema"
                    ));
        }

        // Validar força da senha
        if (!validarSenhaForte(data.senha())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErroLoginDTO(
                            "Senha fraca",
                            "A senha deve ter pelo menos 8 caracteres, incluindo: maiúsculas, minúsculas, números e caracteres especiais"
                    ));
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setNome(data.nome());
        novoUsuario.setEmail(data.email());
        novoUsuario.setSenha(passwordEncoder.encode(data.senha()));
        novoUsuario.setAtivo(true);

        usuarioRepository.save(novoUsuario);
        logger.info("Usuário registrado com sucesso: {}", data.email());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegistroSucessoDTO(
                        "Usuário registrado com sucesso",
                        "Você pode fazer login agora"
                ));
    }

    /**
     * Registra uma tentativa de login falhada
     */
    private void registrarTentativaFalhada(String email) {
        Optional<TentativaLogin> tentativaOpt = tentativaLoginRepository.findByEmail(email);

        TentativaLogin tentativa;
        if (tentativaOpt.isPresent()) {
            tentativa = tentativaOpt.get();
            tentativa.setTentativas(tentativa.getTentativas() + 1);
        } else {
            tentativa = new TentativaLogin();
            tentativa.setEmail(email);
            tentativa.setTentativas(1);
        }

        tentativa.setUltimaTentativa(LocalDateTime.now());

        // Se atingir o máximo de tentativas, bloquear
        if (tentativa.getTentativas() >= MAX_TENTATIVAS) {
            tentativa.setBloqueado(true);
            tentativa.setDataBloqueio(LocalDateTime.now());
            logger.warn("Conta bloqueada após múltiplas tentativas falhas: {}", email);
        }

        tentativaLoginRepository.save(tentativa);
    }

    /**
     * Valida a força da senha
     */
    private boolean validarSenhaForte(String senha) {
        // Mínimo 8 caracteres
        if (senha.length() < 8) {
            return false;
        }

        // Deve conter pelo menos uma letra maiúscula
        if (!senha.matches(".*[A-Z].*")) {
            return false;
        }

        // Deve conter pelo menos uma letra minúscula
        if (!senha.matches(".*[a-z].*")) {
            return false;
        }

        // Deve conter pelo menos um número
        if (!senha.matches(".*[0-9].*")) {
            return false;
        }

        // Deve conter pelo menos um caractere especial
        if (!senha.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            return false;
        }

        return true;
    }

    // ===== DTOS INTERNOS =====

    public record LoginResponseDTO(
            String accessToken,
            String refreshToken,
            String tipo,
            Long expiracaoEm
    ) {}

    public record RefreshTokenDTO(
            String refreshToken
    ) {}

    public record ErroLoginDTO(
            String erro,
            String mensagem
    ) {}

    public record RegistroSucessoDTO(
            String mensagem,
            String detalhes
    ) {}
}