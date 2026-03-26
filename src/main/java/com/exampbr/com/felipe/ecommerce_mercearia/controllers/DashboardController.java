package com.exampbr.com.felipe.ecommerce_mercearia.controllers;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.DashboardResponseDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.PedidoRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/dashboard")
@Tag(name = "Dashboard", description = "Endpoints para obter resumo do dashboard")
public class DashboardController {

    @Autowired
    private PedidoRepository pedidoRepository;

    @GetMapping("/resumo")
    @Operation(summary = "Obter resumo do dashboard", description = "Retorna faturamento e quantidade de pedidos")
    public ResponseEntity<DashboardResponseDTO> obterResumo() {
        long qtd = pedidoRepository.count();
        BigDecimal total = BigDecimal.ZERO;

        return ResponseEntity.ok(new DashboardResponseDTO(total, qtd));
    }
}