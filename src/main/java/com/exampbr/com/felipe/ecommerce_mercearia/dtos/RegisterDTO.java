package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import com.exampbr.com.felipe.ecommerce_mercearia.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterDTO(
        @NotBlank(message = "O nome é obrigatório")
        String nome,

        @NotBlank(message = "O email é obrigatório")
        @Email(message = "Formato de email inválido")
        String email,

        @NotBlank(message = "A senha é obrigatória")
        String senha,

        String fotoPerfil, // Opcional

        @NotNull(message = "O perfil de acesso (Role) é obrigatório")
        Role role
) {
}