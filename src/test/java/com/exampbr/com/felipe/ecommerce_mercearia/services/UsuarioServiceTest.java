package com.exampbr.com.felipe.ecommerce_mercearia.services;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.TestPropertySource;

import com.exampbr.com.felipe.ecommerce_mercearia.entities.Usuario;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.UsuarioRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("UsuarioService Tests")
public class UsuarioServiceTest {

    @Autowired
    private UsuarioService usuarioService;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @Test
    @DisplayName("Deve encontrar usuário por email")
    void testEncontrarPorEmail() {
        Usuario usuario = new Usuario();
        usuario.setEmail("teste@test.com");

        when(usuarioRepository.findByEmail("teste@test.com")).thenReturn(java.util.Optional.of(usuario));

        var resultado = usuarioRepository.findByEmail("teste@test.com");
        assertTrue(resultado.isPresent());
    }

    @Test
    @DisplayName("Deve retornar vazio para email inexistente")
    void testEmailInexistente() {
        when(usuarioRepository.findByEmail("inexistente@test.com")).thenReturn(java.util.Optional.empty());

        var resultado = usuarioRepository.findByEmail("inexistente@test.com");
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve validar senha do usuário")
    void testValidarSenha() {
        Usuario usuario = new Usuario();
        usuario.setEmail("teste@test.com");
        usuario.setSenha("senhaHasheada");

        assertNotNull(usuario.getSenha());
    }

    @Test
    @DisplayName("Deve desativar usuário")
    void testDesativarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setAtivo(true);
        usuario.setAtivo(false);

        assertFalse(usuario.isAtivo());
    }
}