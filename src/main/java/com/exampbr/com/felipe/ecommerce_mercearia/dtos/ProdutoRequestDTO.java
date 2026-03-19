package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record ProdutoRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        @Size(min = 3, max = 150, message = "Nome deve ter entre 3 e 150 caracteres")
        String nome,

        @Size(max = 1000, message = "Descrição deve ter no máximo 1000 caracteres")
        String descricao,

        @NotNull(message = "Preço é obrigatório")
        @DecimalMin(value = "0.01", message = "Preço deve ser maior que 0")
        BigDecimal preco,

        @NotNull(message = "Estoque é obrigatório")
        @PositiveOrZero(message = "Estoque não pode ser negativo")
        Integer estoque,

        @NotNull(message = "ID da categoria é obrigatório")
        @Positive(message = "ID da categoria deve ser positivo")
        Long categoriaId
) {}