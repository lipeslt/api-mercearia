package com.exampbr.com.felipe.ecommerce_mercearia.services;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.PaymentRequestDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Payment;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Pedido;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.PaymentRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.PedidoRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.enums.PaymentStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    public Payment createPaymentPreference(PaymentRequestDTO dto) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(dto.pedidoId());

        if (pedidoOpt.isEmpty()) {
            throw new IllegalArgumentException("Pedido não encontrado");
        }

        Payment payment = new Payment();
        payment.setPedido(pedidoOpt.get());
        payment.setStatus(PaymentStatus.PENDING);

        return paymentRepository.save(payment);
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public Optional<Payment> getPaymentByPedidoId(UUID pedidoId) {
        return paymentRepository.findByPedidoId(pedidoId);
    }

    public void processWebhookPayment(String data) {
        // Implementação futura para processar webhooks do Mercado Pago
    }
}