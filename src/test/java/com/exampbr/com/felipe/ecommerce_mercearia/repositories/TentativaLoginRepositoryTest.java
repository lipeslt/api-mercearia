package com.exampbr.com.felipe.ecommerce_mercearia.repositories;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import com.exampbr.com.felipe.ecommerce_mercearia.entities.TentativaLogin;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("TentativaLoginRepository Tests")
public class TentativaLoginRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TentativaLoginRepository tentativaLoginRepository;

    @Test
    @DisplayName("Deve salvar tentativa de login")
    void testSalvarTentativaLogin() {
        TentativaLogin tentativa = new TentativaLogin();
        tentativa.setEmail("teste@test.com");
        tentativa.setTentativas(1);
        tentativa.setUltimaTentativa(LocalDateTime.now());
        tentativa.setBloqueada(false);

        entityManager.persistAndFlush(tentativa);

        assertNotNull(tentativa.getId());
    }

    @Test
    @DisplayName("Deve encontrar tentativas por email")
    void testEncontrarPorEmail() {
        TentativaLogin tentativa = new TentativaLogin();
        tentativa.setEmail("teste@test.com");
        tentativa.setTentativas(1);

        entityManager.persistAndFlush(tentativa);

        var resultado = tentativaLoginRepository.findByEmail("teste@test.com");
        assertTrue(resultado.isPresent());
    }

    @Test
    @DisplayName("Deve não encontrar email inexistente")
    void testNaoEncontrarEmailInexistente() {
        var resultado = tentativaLoginRepository.findByEmail("inexistente@test.com");
        assertTrue(resultado.isEmpty());
    }

    @Test
    @DisplayName("Deve incrementar tentativas")
    void testAtualizarTentativas() {
        TentativaLogin tentativa = new TentativaLogin();
        tentativa.setEmail("teste@test.com");
        tentativa.setTentativas(3);

        entityManager.persistAndFlush(tentativa);

        tentativa.setTentativas(4);
        entityManager.persistAndFlush(tentativa);

        var resultado = tentativaLoginRepository.findByEmail("teste@test.com");
        assertEquals(4, resultado.get().getTentativas());
    }

    @Test
    @DisplayName("Deve bloquear conta após tentativas")
    void testBloquearContaAposTentativas() {
        TentativaLogin tentativa = new TentativaLogin();
        tentativa.setEmail("teste@test.com");
        tentativa.setTentativas(5);
        tentativa.setBloqueada(true);

        entityManager.persistAndFlush(tentativa);

        var resultado = tentativaLoginRepository.findByEmail("teste@test.com");
        assertTrue(resultado.get().isBloqueada());
    }

    @Test
    @DisplayName("Deve limpar tentativas após sucesso")
    void testLimparTentativasAposSucesso() {
        TentativaLogin tentativa = new TentativaLogin();
        tentativa.setEmail("teste@test.com");
        tentativa.setTentativas(3);

        entityManager.persistAndFlush(tentativa);

        tentativa.setTentativas(0);
        entityManager.persistAndFlush(tentativa);

        var resultado = tentativaLoginRepository.findByEmail("teste@test.com");
        assertEquals(0, resultado.get().getTentativas());
    }
}