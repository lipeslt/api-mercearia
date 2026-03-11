package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class CarrosselDTO {
    public record Request(
            @NotBlank(message = "A URL da imagem é obrigatória") String imageUrl,
            String tituloOpcional,
            @NotNull(message = "A ordem de exibição é obrigatória") Integer ordemExibicao
    ) {}

    public record Response(UUID id, String imageUrl, String tituloOpcional, Integer ordemExibicao) {}
}