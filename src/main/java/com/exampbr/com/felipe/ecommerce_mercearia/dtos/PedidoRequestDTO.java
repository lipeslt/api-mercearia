package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

public record PedidoRequestDTO(
        @NotNull(message = "ID do usuário é obrigatório")
        UUID usuarioId,

        BigDecimal valorTotal
) {}
