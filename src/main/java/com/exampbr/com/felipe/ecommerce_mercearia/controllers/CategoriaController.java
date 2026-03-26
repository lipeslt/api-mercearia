package com.exampbr.com.felipe.ecommerce_mercearia.controllers;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.CategoriaRequestDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Categoria;
import com.exampbr.com.felipe.ecommerce_mercearia.services.CategoriaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/categorias")
@Tag(name = "Categorias", description = "Endpoints para gerenciamento de categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    @Operation(summary = "Listar categorias", description = "Retorna uma lista paginada de categorias ativas")
    public ResponseEntity<Page<Categoria>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "nome") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(categoriaService.listar(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar categoria por ID", description = "Retorna uma categoria específica (se ativa)")
    public ResponseEntity<Categoria> buscarPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(categoriaService.buscarPorId(id));
    }

    @PostMapping
    @Operation(summary = "Criar categoria", description = "Cria uma nova categoria")
    public ResponseEntity<Categoria> criar(@Valid @RequestBody CategoriaRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoriaService.criar(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar categoria", description = "Atualiza uma categoria existente")
    public ResponseEntity<Categoria> atualizar(@PathVariable UUID id, @Valid @RequestBody CategoriaRequestDTO dto) {
        return ResponseEntity.ok(categoriaService.atualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar categoria (Soft Delete)", description = "Marca categoria como inativa")
    public ResponseEntity<Void> deletar(@PathVariable UUID id) {
        categoriaService.deletar(id);
        return ResponseEntity.noContent().build();
    }
}