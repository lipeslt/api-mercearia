package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import java.math.BigDecimal;

public record DashboardResponseDTO(
        BigDecimal faturamentoTotal,
        Long quantidadeVendas
) {
}