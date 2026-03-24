package com.exampbr.com.felipe.ecommerce_mercearia.services;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.CategoriaRequestDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Categoria;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.CategoriaRepository;
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
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Cacheable("categorias")
    public Page<Categoria> listar(Pageable pageable) {
        return categoriaRepository.findAllAtivas(pageable);
    }

    @Cacheable(value = "categoria", key = "#id")
    public Categoria buscarPorId(UUID id) {
        return categoriaRepository.findByIdAtivo(id)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada"));
    }

    @CacheEvict(value = {"categorias", "categoria"}, allEntries = true)
    @Transactional
    public Categoria criar(CategoriaRequestDTO dto) {
        Categoria categoria = new Categoria();
        categoria.setNome(dto.nome());
        categoria.setDescricao(dto.descricao());
        categoria.setAtivo(true);
        return categoriaRepository.save(categoria);
    }

    @CacheEvict(value = {"categorias", "categoria"}, allEntries = true)
    @Transactional
    public Categoria atualizar(UUID id, CategoriaRequestDTO dto) {
        Categoria categoria = buscarPorId(id);
        categoria.setNome(dto.nome());
        categoria.setDescricao(dto.descricao());
        return categoriaRepository.save(categoria);
    }

    @CacheEvict(value = {"categorias", "categoria"}, allEntries = true)
    @Transactional
    public void deletar(UUID id) {
        Categoria categoria = buscarPorId(id);
        // Soft Delete
        categoria.desativar();
        categoriaRepository.save(categoria);
    }
}