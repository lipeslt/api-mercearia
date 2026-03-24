package com.exampbr.com.felipe.ecommerce_mercearia.services;

import com.exampbr.com.felipe.ecommerce_mercearia.config.MercadoPagoClient;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.PaymentRequestDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.PaymentResponseDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.PreferenceDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.enums.PaymentStatus;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Payment;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Pedido;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.PaymentRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.PedidoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PedidoRepository pedidoRepository;
    private final MercadoPagoClient mercadoPagoClient;

    @Transactional
    public PreferenceDTO createPaymentPreference(PaymentRequestDTO paymentRequest) {
        try {
            // Validar pedido
            Pedido pedido = pedidoRepository.findById(paymentRequest.pedidoId())
                    .orElseThrow(() -> new RuntimeException("Pedido não encontrado"));

            // Construir dados para preferência
            Map<String, Object> preferenceData = buildPreferenceData(pedido, paymentRequest);

            // Criar preferência no Mercado Pago
            Map<String, Object> response = mercadoPagoClient.createPreference(preferenceData);

            String preferenceId = (String) response.get("id");
            String initPoint = (String) response.get("init_point");
            String sandboxInitPoint = (String) response.get("sandbox_init_point");

            // Salvar pagamento no banco de dados
            Payment payment = new Payment();
            payment.setPedido(pedido);
            payment.setMercadoPagoId(preferenceId);
            payment.setAmount(paymentRequest.amount());
            payment.setStatus(PaymentStatus.PENDING);
            payment.setPaymentMethod(paymentRequest.paymentMethod());

            paymentRepository.save(payment);

            log.info("Preferência de pagamento criada: {}", preferenceId);

            return new PreferenceDTO(preferenceId, initPoint, sandboxInitPoint);

        } catch (Exception e) {
            log.error("Erro ao criar preferência de pagamento", e);
            throw new RuntimeException("Erro ao criar preferência de pagamento: " + e.getMessage(), e);
        }
    }

    @Transactional
    public PaymentResponseDTO updatePaymentStatus(String mercadoPagoId, PaymentStatus status) {
        Payment payment = paymentRepository.findByMercadoPagoId(mercadoPagoId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));

        payment.setStatus(status);
        Payment updatedPayment = paymentRepository.save(payment);

        return convertToDTO(updatedPayment);
    }

    public PaymentResponseDTO getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pagamento não encontrado"));
        return convertToDTO(payment);
    }

    public PaymentResponseDTO getPaymentByPedidoId(java.util.UUID pedidoId) {
        Payment payment = paymentRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new RuntimeException("Nenhum pagamento encontrado para este pedido"));
        return convertToDTO(payment);
    }

    @Transactional
    public void processWebhookPayment(String paymentId) {
        try {
            Map<String, Object> paymentInfo = mercadoPagoClient.getPaymentInfo(paymentId);
            String status = (String) paymentInfo.get("status");

            PaymentStatus paymentStatus = switch (status) {
                case "approved" -> PaymentStatus.APPROVED;
                case "rejected" -> PaymentStatus.REJECTED;
                case "cancelled" -> PaymentStatus.CANCELLED;
                case "refunded" -> PaymentStatus.REFUNDED;
                default -> PaymentStatus.PENDING;
            };

            // Encontrar pagamento por mercado pago ID ou atualizar todos com pending
            paymentRepository.findByMercadoPagoId(paymentId)
                    .ifPresent(payment -> {
                        payment.setStatus(paymentStatus);
                        paymentRepository.save(payment);
                        log.info("Pagamento {} atualizado para status: {}", paymentId, paymentStatus);
                    });

        } catch (Exception e) {
            log.error("Erro ao processar webhook de pagamento", e);
        }
    }

    private Map<String, Object> buildPreferenceData(Pedido pedido, PaymentRequestDTO paymentRequest) {
        Map<String, Object> preference = new HashMap<>();

        // Items
        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("title", "Pedido #" + pedido.getId());
        item.put("description", paymentRequest.description() != null ?
                paymentRequest.description() : "Compra na Mercearia");
        item.put("quantity", 1);
        item.put("unit_price", paymentRequest.amount().doubleValue());
        items.add(item);

        preference.put("items", items);
        preference.put("external_reference", String.valueOf(pedido.getId()));
        preference.put("notification_url", "https://seu-dominio.com/api/payments/webhook");
        preference.put("auto_return", "approved");

        return preference;
    }

    private PaymentResponseDTO convertToDTO(Payment payment) {
        return new PaymentResponseDTO(
                payment.getId(),
                payment.getPedido().getId().toString(), // Converte UUID para String
                payment.getMercadoPagoId(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getCreatedAt()
        );
    }
}