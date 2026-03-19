package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import java.time.LocalDateTime;

public record TentativaLoginDTO(
        Long id,
        String email,
        Integer tentativas,
        LocalDateTime ultimaTentativa,
        Boolean bloqueado,
        LocalDateTime dataBloqueio
) {}