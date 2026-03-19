package com.exampbr.com.felipe.ecommerce_mercearia.repositories;

import com.exampbr.com.felipe.ecommerce_mercearia.models.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
}