package com.exampbr.com.felipe.ecommerce_mercearia.controllers;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.PaymentRequestDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.PaymentResponseDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Payment;
import com.exampbr.com.felipe.ecommerce_mercearia.services.PaymentService;
import com.exampbr.com.felipe.ecommerce_mercearia.services.WebhookSignatureValidator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payments", description = "Endpoints para gerenciar pagamentos")
@Slf4j
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private WebhookSignatureValidator webhookSignatureValidator;

    @PostMapping
    @Operation(summary = "Criar preferência de pagamento")
    public ResponseEntity<PaymentResponseDTO> createPaymentPreference(@Valid @RequestBody PaymentRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(paymentService.createPaymentPreference(dto));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter pagamento por ID")
    public ResponseEntity<Payment> getPaymentById(@PathVariable Long id) {
        Optional<Payment> payment = paymentService.getPaymentById(id);
        return payment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/pedido/{pedidoId}")
    @Operation(summary = "Obter pagamento por ID do pedido")
    public ResponseEntity<Payment> getPaymentByPedidoId(@PathVariable UUID pedidoId) {
        Optional<Payment> payment = paymentService.getPaymentByPedidoId(pedidoId);
        return payment.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/webhook")
    @Operation(summary = "Processar webhook de pagamento")
    public ResponseEntity<Void> processWebhookPayment(
            @RequestHeader Map<String, String> headers,
            @RequestParam Map<String, String> params,
            @RequestBody String data) {

        if (!webhookSignatureValidator.isValid(headers, params)) {
            log.warn("Webhook recebido com assinatura inválida. Headers: {}", headers);
            return ResponseEntity.badRequest().build();
        }

        paymentService.processWebhookPayment(data);
        return ResponseEntity.ok().build();
    }
}
