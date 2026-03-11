package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record PedidoRequestDTO(
        @NotEmpty(message = "O pedido deve conter pelo menos um item")
        @Valid // Valida a lista de itens internamente
        List<ItemPedidoRequestDTO> itens
) {
}