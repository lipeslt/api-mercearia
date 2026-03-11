package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import java.util.UUID;

public record CategoriaResponseDTO(
        UUID id,
        String nome,
        String descricao
) {
}