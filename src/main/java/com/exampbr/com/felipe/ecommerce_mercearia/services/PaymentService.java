package com.exampbr.com.felipe.ecommerce_mercearia.services;

import com.exampbr.com.felipe.ecommerce_mercearia.config.MercadoPagoClient;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.PaymentRequestDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.dtos.PaymentResponseDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.enums.PaymentStatus;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Payment;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Pedido;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.PaymentRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.PedidoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private MercadoPagoClient mercadoPagoClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.backend-url:http://localhost:8080/api}")
    private String backendUrl;

    public PaymentResponseDTO createPaymentPreference(PaymentRequestDTO dto) {
        Optional<Pedido> pedidoOpt = pedidoRepository.findById(dto.pedidoId());

        if (pedidoOpt.isEmpty()) {
            throw new IllegalArgumentException("Pedido não encontrado");
        }

        Pedido pedido = pedidoOpt.get();

        // Montar items
        Map<String, Object> item = new HashMap<>();
        item.put("id", "pedido-" + pedido.getId());
        item.put("title", "Pedido #" + pedido.getId());
        item.put("quantity", 1);
        item.put("unit_price", pedido.getValorTotal());
        item.put("currency_id", "BRL");

        List<Map<String, Object>> items = new ArrayList<>();
        items.add(item);

        // Montar back_urls
        Map<String, String> backUrls = new HashMap<>();
        backUrls.put("success", frontendUrl + "/pagamento/retorno?collection_status=approved");
        backUrls.put("failure", frontendUrl + "/pagamento/retorno?collection_status=failure");
        backUrls.put("pending", frontendUrl + "/pagamento/retorno?collection_status=pending");

        // Montar payload completo
        Map<String, Object> payload = new HashMap<>();
        payload.put("items", items);
        payload.put("back_urls", backUrls);
        payload.put("auto_approve", false);
        payload.put("external_reference", pedido.getId().toString());
        payload.put("notification_url", backendUrl + "/payments/webhook");

        try {
            Map<String, Object> response = mercadoPagoClient.createPreference(payload);

            Object preferenceId = response.get("id");
            Object initPoint = response.get("init_point");

            Payment payment = new Payment();
            payment.setPedido(pedido);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setMercadoPagoId(preferenceId.toString());
            payment.setAmount(pedido.getValorTotal());
            payment.setPaymentMethod(dto.metodo());
            paymentRepository.save(payment);

            return new PaymentResponseDTO(initPoint.toString(), preferenceId.toString());
        } catch (Exception e) {
            log.error("Erro ao criar preferência no Mercado Pago: {}", e.getMessage());
            throw new RuntimeException("Erro ao criar preferência de pagamento: " + e.getMessage(), e);
        }
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public Optional<Payment> getPaymentByPedidoId(UUID pedidoId) {
        return paymentRepository.findByPedidoId(pedidoId);
    }

    public void processWebhookPayment(String data) {
        try {
            Map<String, Object> payload = objectMapper.readValue(data, Map.class);
            String type = (String) payload.get("type");
            if (!"payment".equals(type)) return;

            Map<String, Object> dataObj = (Map<String, Object>) payload.get("data");
            String mpPaymentId = dataObj.get("id").toString();

            Map<String, Object> paymentInfo = mercadoPagoClient.getPaymentInfo(mpPaymentId);
            String mpStatus = (String) paymentInfo.get("status");
            PaymentStatus newStatus = mapMpStatus(mpStatus);

            String externalRef = (String) paymentInfo.get("external_reference");
            if (externalRef != null) {
                try {
                    UUID pedidoId = UUID.fromString(externalRef);
                    Optional<Payment> paymentOpt = paymentRepository.findByPedidoId(pedidoId);
                    if (paymentOpt.isPresent()) {
                        Payment payment = paymentOpt.get();
                        // Idempotência: só atualiza se o status mudou
                        if (payment.getStatus() != newStatus) {
                            payment.setStatus(newStatus);
                            payment.setMercadoPagoId(mpPaymentId);
                            paymentRepository.save(payment);
                            log.info("Payment {} atualizado para status {}", mpPaymentId, newStatus);
                        } else {
                            log.info("Payment {} já está com status {}. Ignorando.", mpPaymentId, newStatus);
                        }
                    } else {
                        log.warn("Payment não encontrado para pedidoId: {}", pedidoId);
                    }
                } catch (IllegalArgumentException e) {
                    log.error("external_reference inválido: {}", externalRef);
                }
            }
        } catch (Exception e) {
            log.error("Erro ao processar webhook: {}", e.getMessage());
            throw new RuntimeException("Erro ao processar webhook", e);
        }
    }

    private PaymentStatus mapMpStatus(String mpStatus) {
        return switch (mpStatus) {
            case "approved" -> PaymentStatus.APPROVED;
            case "rejected" -> PaymentStatus.REJECTED;
            case "cancelled" -> PaymentStatus.CANCELLED;
            default -> PaymentStatus.PENDING;
        };
    }
}
