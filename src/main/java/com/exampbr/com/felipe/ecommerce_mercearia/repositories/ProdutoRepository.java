package com.exampbr.com.felipe.ecommerce_mercearia.repositories;

import com.exampbr.com.felipe.ecommerce_mercearia.models.Produto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, UUID> {

    @Query("SELECT p FROM Produto p WHERE p.categoria.id = ?1 AND p.ativo = true")
    Page<Produto> findByCategoriaAtivo(UUID categoriaId, Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE p.id = ?1 AND p.ativo = true")
    Optional<Produto> findByIdAtivo(UUID id);
}