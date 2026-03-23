package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import com.exampbr.com.felipe.ecommerce_mercearia.enums.PaymentStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponseDTO(
        Long id,
        Long pedidoId,
        String mercadoPagoId,
        BigDecimal amount,
        PaymentStatus status,
        String paymentMethod,
        LocalDateTime createdAt
) {}