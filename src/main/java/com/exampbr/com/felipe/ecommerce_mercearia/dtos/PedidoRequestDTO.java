package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PedidoRequestDTO(
        @NotNull(message = "ID do usuário é obrigatório")
        @Positive(message = "ID do usuário deve ser positivo")
        Long usuarioId,

        String status
) {}