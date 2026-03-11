package com.exampbr.com.felipe.ecommerce_mercearia.controllers;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.DashboardResponseDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.PedidoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final PedidoRepository pedidoRepository;

    public DashboardController(PedidoRepository pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @GetMapping("/resumo")
    public ResponseEntity<DashboardResponseDTO> obterResumo() {
        BigDecimal total = pedidoRepository.calcularFaturamentoTotal();
        if (total == null) {
            total = BigDecimal.ZERO;
        }
        Long qtd = pedidoRepository.contarPedidosValidos();
        return ResponseEntity.ok(new DashboardResponseDTO(total, qtd));
    }
}