package com.exampbr.com.felipe.ecommerce_mercearia.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

@Service
@Slf4j
public class WebhookSignatureValidator {

    @Value("${mercadopago.webhook.secret:webhook-secret-change-in-production}")
    private String webhookSecret;

    /**
     * Valida a assinatura do webhook do Mercado Pago.
     * Documentação: https://www.mercadopago.com.br/developers/pt/docs/your-integrations/notifications/webhooks
     *
     * O header x-signature tem o formato: ts=<timestamp>,v1=<hash>
     */
    public boolean isValid(Map<String, String> headers, Map<String, String> params) {
        try {
            String xSignature = headers.get("x-signature");
            String xRequestId = headers.get("x-request-id");
            String dataId = params.get("data.id");

            if (xSignature == null || xSignature.isBlank()) {
                log.warn("Webhook recebido sem x-signature");
                return false;
            }

            // Extrair ts e v1 do header x-signature
            String ts = null;
            String v1 = null;
            for (String part : xSignature.split(",")) {
                String[] kv = part.split("=", 2);
                if (kv.length == 2) {
                    if ("ts".equals(kv[0].trim())) ts = kv[1].trim();
                    if ("v1".equals(kv[0].trim())) v1 = kv[1].trim();
                }
            }

            if (ts == null || v1 == null) {
                log.warn("x-signature malformado: {}", xSignature);
                return false;
            }

            // Construir o template: id:{data.id};request-id:{x-request-id};ts:{ts};
            StringBuilder template = new StringBuilder();
            if (dataId != null && !dataId.isBlank()) {
                template.append("id:").append(dataId).append(";");
            }
            if (xRequestId != null && !xRequestId.isBlank()) {
                template.append("request-id:").append(xRequestId).append(";");
            }
            template.append("ts:").append(ts).append(";");

            // Calcular HMAC-SHA256
            String computed = hmacSha256(webhookSecret, template.toString());
            boolean valid = computed.equals(v1);

            if (!valid) {
                log.warn("Assinatura inválida. Esperado: {}, Recebido: {}", computed, v1);
            }

            return valid;
        } catch (Exception e) {
            log.error("Erro ao validar assinatura do webhook: {}", e.getMessage());
            return false;
        }
    }

    private String hmacSha256(String secret, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(hash);
    }
}
