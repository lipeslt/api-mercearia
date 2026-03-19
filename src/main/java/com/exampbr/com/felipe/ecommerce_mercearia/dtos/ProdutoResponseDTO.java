package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProdutoResponseDTO(
        Long id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer estoque,
        Long categoriaId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}