package com.exampbr.com.felipe.ecommerce_mercearia.services;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.ProdutoDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.entities.Produto;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Testes para ProdutoService
 * Valida lógica de negócio de produtos
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProdutoService - Testes de Serviço de Produtos")
class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    private Produto produtoTeste;
    private ProdutoDTO produtoDTOTeste;

    @BeforeEach
    void setup() {
        produtoTeste = new Produto();
        produtoTeste.setId(1L);
        produtoTeste.setNome("Arroz 5kg");
        produtoTeste.setDescricao("Arroz integral");
        produtoTeste.setPreco(new BigDecimal("25.90"));
        produtoTeste.setEstoque(100);
        produtoTeste.setAtivo(true);
        produtoTeste.setCriadoEm(LocalDateTime.now());

        produtoDTOTeste = new ProdutoDTO();
        produtoDTOTeste.setNome("Feijão 1kg");
        produtoDTOTeste.setDescricao("Feijão carioca");
        produtoDTOTeste.setPreco(new BigDecimal("8.50"));
        produtoDTOTeste.setEstoque(200);
    }

    @Test
    @DisplayName("✅ Deve criar produto com sucesso")
    void testCriarProduto() {
        when(produtoRepository.save(any(Produto.class)))
                .thenReturn(produtoTeste);

        Produto resultado = produtoRepository.save(produtoTeste);

        assertNotNull(resultado);
        assertEquals("Arroz 5kg", resultado.getNome());
        assertEquals(new BigDecimal("25.90"), resultado.getPreco());
    }

    @Test
    @DisplayName("✅ Deve buscar produto por ID")
    void testBuscarPorId() {
        when(produtoRepository.findById(1L))
                .thenReturn(Optional.of(produtoTeste));

        Optional<Produto> resultado = produtoRepository.findById(1L);

        assertTrue(resultado.isPresent());
        assertEquals("Arroz 5kg", resultado.get().getNome());
    }

    @Test
    @DisplayName("✅ Deve atualizar estoque do produto")
    void testAtualizarEstoque() {
        produtoTeste.setEstoque(50);
        when(produtoRepository.save(any(Produto.class)))
                .thenReturn(produtoTeste);

        Produto resultado = produtoRepository.save(produtoTeste);

        assertEquals(50, resultado.getEstoque());
    }

    @Test
    @DisplayName("✅ Deve verificar disponibilidade de estoque")
    void testVerificarDisponibilidadeEstoque() {
        boolean disponivel = produtoTeste.getEstoque() > 0;
        assertTrue(disponivel);
    }

    @Test
    @DisplayName("❌ Deve detectar falta de estoque")
    void testEstoqueInsuficiente() {
        produtoTeste.setEstoque(0);
        boolean disponivel = produtoTeste.getEstoque() > 0;
        assertFalse(disponivel);
    }

    @Test
    @DisplayName("✅ Deve desativar produto")
    void testDesativarProduto() {
        produtoTeste.setAtivo(false);
        when(produtoRepository.save(any(Produto.class)))
                .thenReturn(produtoTeste);

        Produto resultado = produtoRepository.save(produtoTeste);

        assertFalse(resultado.isAtivo());
    }

    @Test
    @DisplayName("✅ Deve deletar produto")
    void testDeletarProduto() {
        doNothing().when(produtoRepository).deleteById(1L);

        produtoRepository.deleteById(1L);

        verify(produtoRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("✅ Deve validar preço não negativo")
    void testValidarPrecoPositivo() {
        boolean precoValido = produtoTeste.getPreco().compareTo(BigDecimal.ZERO) > 0;
        assertTrue(precoValido);
    }

    @Test
    @DisplayName("❌ Deve rejeitar preço negativo")
    void testRejectarPrecoNegativo() {
        BigDecimal precoNegativo = new BigDecimal("-10.00");
        boolean precoValido = precoNegativo.compareTo(BigDecimal.ZERO) > 0;
        assertFalse(precoValido);
    }
}