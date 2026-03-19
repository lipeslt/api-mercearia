package com.exampbr.com.felipe.ecommerce_mercearia.controllers;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.LoginRequestDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.RegisterRequestDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.entities.Usuario;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.UsuarioRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.services.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes para AuthController
 * Valida autenticação, registro e tokens JWT
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController - Testes de Autenticação")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private TokenService tokenService;

    private Usuario usuarioTeste;
    private RegisterRequestDTO registerDTO;
    private LoginRequestDTO loginDTO;

    @BeforeEach
    void setup() {
        // Setup usuário de teste
        usuarioTeste = new Usuario();
        usuarioTeste.setId(1L);
        usuarioTeste.setNome("João Silva");
        usuarioTeste.setEmail("joao@example.com");
        usuarioTeste.setSenha(passwordEncoder.encode("SenhaForte123!"));
        usuarioTeste.setRole("CLIENTE");
        usuarioTeste.setAtivo(true);
        usuarioTeste.setCriadoEm(LocalDateTime.now());

        // DTO de registro
        registerDTO = new RegisterRequestDTO();
        registerDTO.setNome("Maria Santos");
        registerDTO.setEmail("maria@example.com");
        registerDTO.setSenha("SenhaForte456!");

        // DTO de login
        loginDTO = new LoginRequestDTO();
        loginDTO.setEmail("joao@example.com");
        loginDTO.setSenha("SenhaForte123!");
    }

    @Test
    @DisplayName("✅ Deve registrar novo usuário com sucesso")
    void testRegistrarNovoUsuario() throws Exception {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuarioTeste);

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email").value("joao@example.com"))
                .andExpect(jsonPath("$.role").value("CLIENTE"));
    }

    @Test
    @DisplayName("❌ Deve rejeitar registro com email duplicado")
    void testRegistrarEmailDuplicado() throws Exception {
        when(usuarioRepository.existsByEmail(anyString())).thenReturn(true);

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("❌ Deve validar email inválido no registro")
    void testRegistrarEmailInvalido() throws Exception {
        registerDTO.setEmail("email-invalido");

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("❌ Deve rejeitar senha fraca no registro")
    void testRegistrarSenhaFraca() throws Exception {
        registerDTO.setSenha("123");  // Muito fraca

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("✅ Deve fazer login com credenciais válidas")
    void testLoginComSucesso() throws Exception {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(usuarioTeste));
        when(tokenService.gerarToken(any(Usuario.class))).thenReturn("jwt-token-valido");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tipo").value("Bearer"));
    }

    @Test
    @DisplayName("❌ Deve rejeitar login com email inexistente")
    void testLoginEmailInexistente() throws Exception {
        when(usuarioRepository.findByEmail(anyString())).thenReturn(java.util.Optional.empty());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("❌ Deve rejeitar login com senha incorreta")
    void testLoginSenhaIncorreta() throws Exception {
        loginDTO.setSenha("SenhaErrada123!");
        when(usuarioRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(usuarioTeste));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("❌ Deve bloquear usuário após 5 tentativas falhas")
    void testBloqueioAposTentativasFalhas() throws Exception {
        loginDTO.setSenha("SenhaErrada!");
        when(usuarioRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(usuarioTeste));

        // 5 tentativas falhas
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(loginDTO)))
                    .andExpect(status().isUnauthorized());
        }

        // 6ª tentativa deve bloquear (423 Locked)
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isLocked());
    }

    @Test
    @DisplayName("✅ Deve renovar token com refresh token válido")
    void testRefreshTokenComSucesso() throws Exception {
        String refreshToken = "refresh-token-valido";
        when(tokenService.validarRefreshToken(refreshToken)).thenReturn(true);
        when(tokenService.extrairUsuarioDoToken(refreshToken)).thenReturn("joao@example.com");
        when(usuarioRepository.findByEmail(anyString())).thenReturn(java.util.Optional.of(usuarioTeste));
        when(tokenService.gerarToken(any(Usuario.class))).thenReturn("novo-access-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + refreshToken + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()));
    }

    @Test
    @DisplayName("❌ Deve rejeitar refresh token inválido")
    void testRefreshTokenInvalido() throws Exception {
        when(tokenService.validarRefreshToken(anyString())).thenReturn(false);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"token-invalido\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("❌ Deve rejeitar refresh token expirado")
    void testRefreshTokenExpirado() throws Exception {
        when(tokenService.validarRefreshToken(anyString())).thenReturn(false);

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"token-expirado\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("✅ Deve fazer logout com sucesso")
    void testLogoutComSucesso() throws Exception {
        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer jwt-token-valido"))
                .andExpect(status().isOk());
    }
}