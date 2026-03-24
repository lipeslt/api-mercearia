package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PaymentRequestDTO(
        @NotNull(message = "Pedido ID não pode ser nulo")
        UUID pedidoId,

        String metodo
) {}