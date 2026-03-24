package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record PedidoRequestDTO(
        @NotNull(message = "ID do usuário é obrigatório")
        UUID usuarioId,

        @Positive(message = "Quantidade deve ser positiva")
        Integer quantidade
) {}