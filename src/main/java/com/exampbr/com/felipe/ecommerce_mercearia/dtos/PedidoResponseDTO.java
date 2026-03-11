package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import com.exampbr.com.felipe.ecommerce_mercearia.enums.StatusPedido;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PedidoResponseDTO(
        UUID id,
        Instant dataCriacao,
        StatusPedido status,
        BigDecimal valorTotal,
        List<ItemPedidoResponseDTO> itens
) {
}