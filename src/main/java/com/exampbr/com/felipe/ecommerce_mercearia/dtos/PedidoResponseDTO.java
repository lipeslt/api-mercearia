package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import java.time.LocalDateTime;

public record PedidoResponseDTO(
        Long id,
        Long usuarioId,
        String status,
        LocalDateTime dataPedido,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}