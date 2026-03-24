package com.exampbr.com.felipe.ecommerce_mercearia.repositories;

import com.exampbr.com.felipe.ecommerce_mercearia.models.Categoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {

    @Query("SELECT c FROM Categoria c WHERE c.ativo = true ORDER BY c.nome ASC")
    Page<Categoria> findAllAtivas(Pageable pageable);

    @Query("SELECT c FROM Categoria c WHERE c.id = ?1 AND c.ativo = true")
    Optional<Categoria> findByIdAtivo(UUID id);
}