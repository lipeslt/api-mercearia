package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoResponseDTO(
        UUID id,
        String nome,
        BigDecimal preco,
        Integer estoque,
        CategoriaResponseDTO categoria // Devolvemos os dados da categoria junto
) {
}