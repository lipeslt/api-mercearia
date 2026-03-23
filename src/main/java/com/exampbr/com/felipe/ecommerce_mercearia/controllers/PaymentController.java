package com.exampbr.com.felipe.ecommerce_mercearia.controllers;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.PaymentRequestDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.PaymentResponseDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.PreferenceDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.services.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Pagamentos", description = "API de Pagamentos com Mercado Pago")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create-preference")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Criar preferência de pagamento", description = "Cria uma preferência de pagamento no Mercado Pago")
    public ResponseEntity<PreferenceDTO> createPaymentPreference(
            @Valid @RequestBody PaymentRequestDTO paymentRequest) {
        PreferenceDTO preference = paymentService.createPaymentPreference(paymentRequest);
        return ResponseEntity.ok(preference);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Obter pagamento por ID", description = "Retorna os detalhes de um pagamento")
    public ResponseEntity<PaymentResponseDTO> getPayment(@PathVariable Long paymentId) {
        PaymentResponseDTO payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/pedido/{pedidoId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Obter pagamento por pedido", description = "Retorna o pagamento associado a um pedido")
    public ResponseEntity<PaymentResponseDTO> getPaymentByPedido(@PathVariable Long pedidoId) {
        PaymentResponseDTO payment = paymentService.getPaymentByPedidoId(pedidoId);
        return ResponseEntity.ok(payment);
    }

    @PostMapping("/webhook")
    @Operation(summary = "Webhook do Mercado Pago", description = "Recebe notificações de pagamento do Mercado Pago")
    public ResponseEntity<String> handleWebhook(
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String type) {

        if ("payment".equals(type) && id != null) {
            paymentService.processWebhookPayment(id);
        }

        return ResponseEntity.ok("Webhook processado");
    }
}