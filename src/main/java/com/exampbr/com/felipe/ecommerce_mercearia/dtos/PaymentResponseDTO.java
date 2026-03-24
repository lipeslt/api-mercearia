package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import com.exampbr.com.felipe.ecommerce_mercearia.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentResponseDTO(
        Long id,
        String pedidoId,
        String mercadoPagoId,
        BigDecimal amount,
        PaymentStatus status,
        String paymentMethod,
        OffsetDateTime createdAt
) {}