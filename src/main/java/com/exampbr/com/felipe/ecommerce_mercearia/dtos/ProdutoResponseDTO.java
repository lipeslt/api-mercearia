package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record ProdutoResponseDTO(
        UUID id,
        String nome,
        String descricao,
        BigDecimal preco,
        Integer estoque,
        UUID categoriaId,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}