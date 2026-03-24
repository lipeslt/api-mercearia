package com.exampbr.com.felipe.ecommerce_mercearia.services;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.ProdutoRequestDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.ProdutoResponseDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Categoria;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Produto;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.CategoriaRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Cacheable("produtos")
    public Page<ProdutoResponseDTO> listar(Pageable pageable) {
        return produtoRepository.findAll(pageable)
                .map(this::convertToResponseDTO);
    }

    @Cacheable(value = "produto", key = "#id")
    public ProdutoResponseDTO buscarPorId(UUID id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));
        return convertToResponseDTO(produto);
    }

    @Cacheable(value = "produtosPorCategoria", key = "#categoriaId")
    public Page<ProdutoResponseDTO> buscarPorCategoria(UUID categoriaId, Pageable pageable) {
        Page<Produto> allProdutos = produtoRepository.findAll(pageable);

        var produtosFiltrados = allProdutos.getContent()
                .stream()
                .filter(p -> p.getCategoria().getId().equals(categoriaId))
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        return new PageImpl<>(produtosFiltrados, pageable, allProdutos.getTotalElements());
    }

    @CacheEvict(value = {"produtos", "produto", "produtosPorCategoria"}, allEntries = true)
    @Transactional
    public ProdutoResponseDTO criar(ProdutoRequestDTO dto) {
        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        Produto produto = new Produto();
        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setPreco(dto.preco());
        produto.setEstoque(dto.estoque());
        produto.setCategoria(categoria);
        produto.setAtivo(true); // Soft Delete: novo produto começa ativo

        return convertToResponseDTO(produtoRepository.save(produto));
    }

    @CacheEvict(value = {"produtos", "produto", "produtosPorCategoria"}, allEntries = true)
    @Transactional
    public ProdutoResponseDTO atualizar(UUID id, ProdutoRequestDTO dto) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

        Categoria categoria = categoriaRepository.findById(dto.categoriaId())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));

        produto.setNome(dto.nome());
        produto.setDescricao(dto.descricao());
        produto.setPreco(dto.preco());
        produto.setEstoque(dto.estoque());
        produto.setCategoria(categoria);

        return convertToResponseDTO(produtoRepository.save(produto));
    }

    @CacheEvict(value = {"produtos", "produto", "produtosPorCategoria"}, allEntries = true)
    @Transactional
    public void deletar(UUID id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado"));

        // Soft Delete: marca como inativo ao invés de deletar fisicamente
        produto.setAtivo(false);
        produtoRepository.save(produto);
    }

    private ProdutoResponseDTO convertToResponseDTO(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getEstoque(),
                produto.getCategoria().getId(),
                convertLocalDateTimeToOffsetDateTime(produto.getCriadoEm()),
                convertLocalDateTimeToOffsetDateTime(produto.getAtualizadoEm())
        );
    }

    /**
     * Converte LocalDateTime para OffsetDateTime usando UTC
     * Necessário porque BaseEntity usa LocalDateTime, mas DTO espera OffsetDateTime
     */
    private OffsetDateTime convertLocalDateTimeToOffsetDateTime(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return localDateTime.atOffset(ZoneOffset.UTC);
    }
}