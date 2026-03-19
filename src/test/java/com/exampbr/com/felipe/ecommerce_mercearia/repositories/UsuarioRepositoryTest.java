package com.exampbr.com.felipe.ecommerce_mercearia.repositories;

import com.exampbr.com.felipe.ecommerce_mercearia.entities.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para UsuarioRepository
 * Testa integração com banco de dados H2
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UsuarioRepository - Testes de Repositório")
class UsuarioRepositoryTest {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario usuarioTeste;

    @BeforeEach
    void setup() {
        usuarioTeste = new Usuario();
        usuarioTeste.setNome("João Silva");
        usuarioTeste.setEmail("joao@example.com");
        usuarioTeste.setSenha("$2a$12$hash_da_senha");
        usuarioTeste.setRole("CLIENTE");
        usuarioTeste.setAtivo(true);
        usuarioTeste.setCriadoEm(LocalDateTime.now());
    }

    @Test
    @DisplayName("✅ Deve salvar novo usuário no banco")
    void testSalvarUsuario() {
        Usuario salvo = usuarioRepository.save(usuarioTeste);

        assertNotNull(salvo.getId());
        assertEquals("João Silva", salvo.getNome());
        assertEquals("joao@example.com", salvo.getEmail());
    }

    @Test
    @DisplayName("✅ Deve buscar usuário por email")
    void testBuscarPorEmail() {
        usuarioRepository.save(usuarioTeste);

        Optional<Usuario> encontrado = usuarioRepository.findByEmail("joao@example.com");

        assertTrue(encontrado.isPresent());
        assertEquals("João Silva", encontrado.get().getNome());
    }

    @Test
    @DisplayName("❌ Deve retornar vazio ao buscar email inexistente")
    void testBuscarEmailInexistente() {
        Optional<Usuario> encontrado = usuarioRepository.findByEmail("inexistente@example.com");

        assertFalse(encontrado.isPresent());
    }

    @Test
    @DisplayName("✅ Deve verificar se email existe")
    void testExisteEmail() {
        usuarioRepository.save(usuarioTeste);

        boolean existe = usuarioRepository.existsByEmail("joao@example.com");

        assertTrue(existe);
    }

    @Test
    @DisplayName("❌ Deve retornar false para email inexistente")
    void testEmailNaoExiste() {
        boolean existe = usuarioRepository.existsByEmail("inexistente@example.com");

        assertFalse(existe);
    }

    @Test
    @DisplayName("✅ Deve atualizar usuário existente")
    void testAtualizarUsuario() {
        Usuario salvo = usuarioRepository.save(usuarioTeste);
        salvo.setNome("João Silva Atualizado");

        Usuario atualizado = usuarioRepository.save(salvo);

        assertEquals("João Silva Atualizado", atualizado.getNome());
    }

    @Test
    @DisplayName("✅ Deve deletar usuário do banco")
    void testDeletarUsuario() {
        Usuario salvo = usuarioRepository.save(usuarioTeste);
        Long id = salvo.getId();

        usuarioRepository.deleteById(id);
        Optional<Usuario> encontrado = usuarioRepository.findById(id);

        assertFalse(encontrado.isPresent());
    }

    @Test
    @DisplayName("✅ Deve contar usuários ativos")
    void testContarUsuarios() {
        usuarioRepository.save(usuarioTeste);

        Usuario usuario2 = new Usuario();
        usuario2.setNome("Maria");
        usuario2.setEmail("maria@example.com");
        usuario2.setSenha("$2a$12$hash");
        usuario2.setAtivo(true);

        usuarioRepository.save(usuario2);

        long total = usuarioRepository.count();

        assertEquals(2, total);
    }
}