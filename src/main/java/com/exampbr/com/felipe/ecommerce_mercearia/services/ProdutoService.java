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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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
        return produtoRepository.findByCategoriaId(categoriaId, pageable)
                .map(this::convertToResponseDTO);
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
        produtoRepository.delete(produto);
    }

    private ProdutoResponseDTO convertToResponseDTO(Produto produto) {
        return new ProdutoResponseDTO(
                produto.getId(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getEstoque(),
                produto.getCategoria().getId(),
                produto.getCriadoEm(),
                produto.getAtualizadoEm()
        );
    }
}