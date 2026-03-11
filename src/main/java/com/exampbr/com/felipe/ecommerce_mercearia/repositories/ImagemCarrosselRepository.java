package com.exampbr.com.felipe.ecommerce_mercearia.repositories;

import com.exampbr.com.felipe.ecommerce_mercearia.models.ImagemCarrossel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ImagemCarrosselRepository extends JpaRepository<ImagemCarrossel, UUID> {
    List<ImagemCarrossel> findAllByOrderByOrdemExibicaoAsc();
}