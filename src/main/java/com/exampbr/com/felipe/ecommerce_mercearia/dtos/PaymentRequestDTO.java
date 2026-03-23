package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record PaymentRequestDTO(
        @NotNull(message = "Pedido ID não pode ser nulo")
        Long pedidoId,

        @NotNull(message = "Valor não pode ser nulo")
        @Positive(message = "Valor deve ser positivo")
        BigDecimal amount,

        @NotNull(message = "Método de pagamento não pode ser nulo")
        String paymentMethod,

        String description
) {}