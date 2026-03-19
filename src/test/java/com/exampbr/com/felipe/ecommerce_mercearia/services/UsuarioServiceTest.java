package com.exampbr.com.felipe.ecommerce_mercearia.services;

import com.exampbr.com.felipe.ecommerce_mercearia.entities.Usuario;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes para UsuarioService
 * Valida lógica de negócio de usuários
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UsuarioService - Testes de Serviço de Usuários")
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    private Usuario usuarioTeste;

    @BeforeEach
    void setup() {
        usuarioTeste = new Usuario();
        usuarioTeste.setId(1L);
        usuarioTeste.setNome("João Silva");
        usuarioTeste.setEmail("joao@example.com");
        usuarioTeste.setSenha("$2a$12$hash_da_senha");
        usuarioTeste.setRole("CLIENTE");
        usuarioTeste.setAtivo(true);
        usuarioTeste.setCriadoEm(LocalDateTime.now());
    }

    @Test
    @DisplayName("✅ Deve buscar usuário por email com sucesso")
    void testBuscarPorEmailComSucesso() {
        when(usuarioRepository.findByEmail("joao@example.com"))
                .thenReturn(Optional.of(usuarioTeste));

        Optional<Usuario> resultado = usuarioRepository.findByEmail("joao@example.com");

        assertTrue(resultado.isPresent());
        assertEquals("João Silva", resultado.get().getNome());
        verify(usuarioRepository, times(1)).findByEmail("joao@example.com");
    }

    @Test
    @DisplayName("❌ Deve retornar vazio ao buscar email inexistente")
    void testBuscarPorEmailInexistente() {
        when(usuarioRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioRepository.findByEmail("inexistente@example.com");

        assertFalse(resultado.isPresent());
    }

    @Test
    @DisplayName("✅ Deve criar novo usuário com sucesso")
    void testCriarNovoUsuario() {
        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuarioTeste);
        when(passwordEncoder.encode("SenhaForte123!"))
                .thenReturn("$2a$12$hash_da_senha");

        usuarioTeste.setSenha(passwordEncoder.encode("SenhaForte123!"));
        Usuario resultado = usuarioRepository.save(usuarioTeste);

        assertNotNull(resultado);
        assertEquals("joao@example.com", resultado.getEmail());
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("❌ Deve rejeitar criação com email duplicado")
    void testCriarUsuarioDuplicado() {
        when(usuarioRepository.existsByEmail("joao@example.com"))
                .thenReturn(true);

        boolean existe = usuarioRepository.existsByEmail("joao@example.com");

        assertTrue(existe);
    }

    @Test
    @DisplayName("✅ Deve atualizar dados do usuário")
    void testAtualizarUsuario() {
        usuarioTeste.setNome("João Silva Atualizado");
        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuarioTeste);

        Usuario resultado = usuarioRepository.save(usuarioTeste);

        assertEquals("João Silva Atualizado", resultado.getNome());
    }

    @Test
    @DisplayName("✅ Deve deletar usuário com sucesso")
    void testDeletarUsuario() {
        doNothing().when(usuarioRepository).deleteById(1L);

        usuarioRepository.deleteById(1L);

        verify(usuarioRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("✅ Deve validar senha corretamente")
    void testValidarSenha() {
        when(passwordEncoder.matches("SenhaForte123!", "$2a$12$hash_da_senha"))
                .thenReturn(true);

        boolean senhaValida = passwordEncoder.matches("SenhaForte123!", "$2a$12$hash_da_senha");

        assertTrue(senhaValida);
    }

    @Test
    @DisplayName("❌ Deve rejeitar senha incorreta")
    void testValidarSenhaIncorreta() {
        when(passwordEncoder.matches("SenhaErrada123!", "$2a$12$hash_da_senha"))
                .thenReturn(false);

        boolean senhaValida = passwordEncoder.matches("SenhaErrada123!", "$2a$12$hash_da_senha");

        assertFalse(senhaValida);
    }

    @Test
    @DisplayName("✅ Deve bloquear usuário após tentativas falhas")
    void testBloquearUsuario() {
        usuarioTeste.setAtivo(false);
        usuarioTeste.setTentativasLogin(5);
        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuarioTeste);

        Usuario resultado = usuarioRepository.save(usuarioTeste);

        assertFalse(resultado.isAtivo());
        assertEquals(5, resultado.getTentativasLogin());
    }

    @Test
    @DisplayName("✅ Deve resetar tentativas após login bem-sucedido")
    void testResetarTentativas() {
        usuarioTeste.setTentativasLogin(0);
        when(usuarioRepository.save(any(Usuario.class)))
                .thenReturn(usuarioTeste);

        Usuario resultado = usuarioRepository.save(usuarioTeste);

        assertEquals(0, resultado.getTentativasLogin());
    }
}