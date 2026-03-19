package com.exampbr.com.felipe.ecommerce_mercearia.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.AuthenticationDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.RegisterDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.entities.Usuario;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.UsuarioRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.services.TokenService;
import com.exampbr.com.felipe.ecommerce_mercearia.services.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("AuthController Tests")
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private TokenService tokenService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Usuario usuarioTeste;

    @BeforeEach
    void setUp() {
        usuarioTeste = new Usuario();
        usuarioTeste.setId(1L);
        usuarioTeste.setNome("Felipe Teste");
        usuarioTeste.setEmail("felipe@test.com");
        usuarioTeste.setSenha(passwordEncoder.encode("senha123"));
        usuarioTeste.setAtivo(true);
    }

    @Test
    @DisplayName("Deve fazer login com sucesso")
    void testLoginComSucesso() throws Exception {
        AuthenticationDTO auth = new AuthenticationDTO("felipe@test.com", "senha123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(auth)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 401 para email inválido")
    void testLoginComEmailInvalido() throws Exception {
        AuthenticationDTO auth = new AuthenticationDTO("inexistente@test.com", "senha123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(auth)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve retornar 401 para senha inválida")
    void testLoginComSenhaInvalida() throws Exception {
        AuthenticationDTO auth = new AuthenticationDTO("felipe@test.com", "senhaErrada");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(auth)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve registrar usuário com sucesso")
    void testRegistroComSucesso() throws Exception {
        RegisterDTO register = new RegisterDTO("Felipe Teste", "novo@test.com", "SenhaForte123!");

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Deve retornar 409 para email duplicado")
    void testRegistroComEmailExistente() throws Exception {
        RegisterDTO register = new RegisterDTO("Felipe Teste", "felipe@test.com", "SenhaForte123!");

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Deve retornar 400 para senha fraca")
    void testRegistroComSenhaFraca() throws Exception {
        RegisterDTO register = new RegisterDTO("Felipe Teste", "novo@test.com", "123");

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Deve fazer refresh de token com sucesso")
    void testRefreshTokenComSucesso() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"refresh_token_valido\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Deve retornar 401 para refresh token inválido")
    void testRefreshTokenInvalido() throws Exception {
        mockMvc.perform(post("/api/auth/refresh")
                        .contentType("application/json")
                        .content("{\"refreshToken\":\"token_invalido\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve bloquear conta após múltiplas tentativas")
    void testBloqueioConta() throws Exception {
        AuthenticationDTO auth = new AuthenticationDTO("felipe@test.com", "senhaErrada");

        // Faz 5 tentativas de login incorretas
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(auth)));
        }

        // Próxima tentativa deve retornar 423 (Locked)
        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(auth)))
                .andExpect(status().isLocked());
    }

    @Test
    @DisplayName("Deve validar email no registro")
    void testRegistroComEmailInvalido() throws Exception {
        RegisterDTO register = new RegisterDTO("Felipe Teste", "email-invalido", "SenhaForte123!");

        mockMvc.perform(post("/api/auth/registrar")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isBadRequest());
    }
}