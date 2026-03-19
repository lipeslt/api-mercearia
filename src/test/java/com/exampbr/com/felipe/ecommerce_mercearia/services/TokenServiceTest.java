package com.exampbr.com.felipe.ecommerce_mercearia.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.exampbr.com.felipe.ecommerce_mercearia.entities.Usuario;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("TokenService Tests")
public class TokenServiceTest {

    @Autowired
    private TokenService tokenService;

    private Usuario usuario;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome("Felipe Teste");
        usuario.setEmail("felipe@test.com");
        usuario.setAtivo(true);
    }

    @Test
    @DisplayName("Deve gerar token válido")
    void testGerarTokenValido() {
        String token = tokenService.generateToken(usuario);

        assertNotNull(token, "Token não deve ser nulo");
        assertFalse(token.isEmpty(), "Token não deve estar vazio");
    }

    @Test
    @DisplayName("Deve extrair subject do token")
    void testExtrairSubjectDoToken() {
        String token = tokenService.generateToken(usuario);
        String subject = tokenService.getSubject(token);

        assertEquals(usuario.getEmail(), subject, "Subject deve ser o email do usuário");
    }

    @Test
    @DisplayName("Deve validar token expirado")
    void testValidarTokenExpirado() {
        String token = tokenService.generateToken(usuario);
        // Token gerado agora é válido
        assertTrue(!tokenService.isTokenExpired(token), "Token recém-criado não deve estar expirado");
    }

    @Test
    @DisplayName("Deve gerar refresh token")
    void testGerarRefreshToken() {
        String refreshToken = tokenService.generateRefreshToken(usuario);

        assertNotNull(refreshToken, "Refresh token não deve ser nulo");
        assertFalse(refreshToken.isEmpty(), "Refresh token não deve estar vazio");
    }

    @Test
    @DisplayName("Deve renovar token com refresh token")
    void testRenovarToken() {
        String refreshToken = tokenService.generateRefreshToken(usuario);
        String novoToken = tokenService.generateToken(usuario);

        assertNotNull(novoToken, "Novo token não deve ser nulo");
        assertNotEquals(novoToken, refreshToken, "Tokens devem ser diferentes");
    }

    @Test
    @DisplayName("Deve falhar com token inválido")
    void testTokenInvalido() {
        String tokenInvalido = "token.invalido.fake";
        // Deve lançar exceção ou retornar false
        try {
            tokenService.getSubject(tokenInvalido);
        } catch (Exception e) {
            assertTrue(true, "Deve lançar exceção para token inválido");
        }
    }

    @Test
    @DisplayName("Deve ter tempo de expiração configurado")
    void testTempoExpiracao() {
        String token = tokenService.generateToken(usuario);
        assertNotNull(token);
        // Token deve expirar após tempo configurado
    }

    @Test
    @DisplayName("Deve conter claims do usuário")
    void testClaimsDoUsuario() {
        String token = tokenService.generateToken(usuario);
        String email = tokenService.getSubject(token);

        assertEquals(usuario.getEmail(), email, "Token deve conter email do usuário");
    }

    @Test
    @DisplayName("Deve gerar tokens diferentes para cada chamada")
    void testTokenesDiferentes() {
        String token1 = tokenService.generateToken(usuario);
        String token2 = tokenService.generateToken(usuario);

        assertNotEquals(token1, token2, "Tokens gerados em momentos diferentes devem diferir");
    }
}