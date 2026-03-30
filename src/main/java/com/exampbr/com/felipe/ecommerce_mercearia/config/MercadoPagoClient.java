package com.exampbr.com.felipe.ecommerce_mercearia.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class MercadoPagoClient {

    private static final String BASE_URL = "https://api.mercadopago.com";

    @Value("${mercadopago.access-token}")
    private String accessToken;

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public Map<String, Object> createPreference(Map<String, Object> preferenceData) throws Exception {
        String url = getBaseUrl() + "/checkout/preferences";

        String requestBody = objectMapper.writeValueAsString(preferenceData);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201 || response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Map.class);
        } else {
            log.error("Erro ao criar preferência: {}", response.body());
            throw new RuntimeException("Erro ao criar preferência de pagamento: " + response.statusCode());
        }
    }

    public Map<String, Object> getPaymentInfo(String paymentId) throws Exception {
        String url = getBaseUrl() + "/v1/payments/" + paymentId;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return objectMapper.readValue(response.body(), Map.class);
        } else {
            log.error("Erro ao obter informações de pagamento: {}", response.body());
            throw new RuntimeException("Erro ao obter informações de pagamento");
        }
    }

    private String getBaseUrl() {
        return BASE_URL;
    }
}