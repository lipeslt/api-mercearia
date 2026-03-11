package com.exampbr.com.felipe.ecommerce_mercearia.exceptions;

import java.time.Instant;
import java.util.List;

public record StandardError(
        Instant timestamp, // Quando o erro aconteceu
        Integer status,    // Código HTTP
        String error,      // Resumo do erro
        String message,    // Mensagem detalhada para o usuário
        String path,       // Qual endpoint gerou o erro
        List<String> validationErrors // Lista de campos que falharam na validação
) {
}