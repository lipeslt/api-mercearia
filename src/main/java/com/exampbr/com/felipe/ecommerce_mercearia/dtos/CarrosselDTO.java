package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import jakarta.validation.constraints.NotBlank;

public record CarrosselDTO(
        @NotBlank(message = "Título é obrigatório")
        String titulo,

        String descricao,

        @NotBlank(message = "URL da imagem é obrigatória")
        String urlImagem,

        boolean ativo
) {}