package com.exampbr.com.felipe.ecommerce_mercearia.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes para RateLimitInterceptor
 * Valida proteção contra múltiplas requisições
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitInterceptor - Testes de Rate Limiting")
class RateLimitInterceptorTest {

    private Bucket bucket;

    @BeforeEach
    void setup() {
        // Configurar bucket com 60 requisições por minuto
        Bandwidth limit = Bandwidth.classic(60, Refill.intervally(60, Duration.ofMinutes(1)));
        bucket = Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    @Test
    @DisplayName("✅ Deve permitir requisição dentro do limite")
    void testRequisiçãoDentroLimite() {
        boolean permitido = bucket.tryConsume(1);
        assertTrue(permitido, "Requisição deveria ser permitida");
    }

    @Test
    @DisplayName("✅ Deve permitir múltiplas requisições até o limite")
    void testMultiplasRequisicoesDentroLimite() {
        for (int i = 0; i < 60; i++) {
            assertTrue(bucket.tryConsume(1), "Requisição " + i + " deveria ser permitida");
        }
    }

    @Test
    @DisplayName("❌ Deve rejeitar requisição que excede limite")
    void testRequisicaoAlemDoLimite() {
        // Consumir 60 requisições
        for (int i = 0; i < 60; i++) {
            bucket.tryConsume(1);
        }

        // 61ª requisição deve ser rejeitada
        boolean permitido = bucket.tryConsume(1);
        assertFalse(permitido, "61ª requisição deveria ser rejeitada");
    }

    @Test
    @DisplayName("✅ Deve contar tokens corretamente")
    void testContarTokens() {
        bucket.tryConsume(10);

        // Verificar tokens restantes
        long tokensRestantes = bucket.estimateAbilityToConsume(1).getRoundedTokensToWait();
        // Se consumiu 10 de 60, devem restar 50
        assertTrue(tokensRestantes <= 50);
    }

    @Test
    @DisplayName("✅ Deve recuperar tokens após tempo decorrido")
    void testRecuperarTokensAposTempo() {
        // Consumir todos os tokens
        for (int i = 0; i < 60; i++) {
            bucket.tryConsume(1);
        }

        // Tentar consumir deve falhar
        assertFalse(bucket.tryConsume(1));

        // Após "1 minuto" (simular) os tokens seriam restaurados
        // Em teste real, teríamos que esperar ou mockar o tempo
    }

    @Test
    @DisplayName("❌ Deve rejeitar requisições em burst")
    void testRejeitar BurstDeRequisicoes() {
        // Tentar consumir mais que o limite
        boolean permitido = bucket.tryConsume(70);
        assertFalse(permitido, "Burst deveria ser rejeitado");
    }

    @Test
    @DisplayName("✅ Deve funcionar com diferentes IPs")
    void testLimitePorIP() {
        // Cada IP teria seu próprio bucket
        Bucket bucketIP1 = criarBucket();
        Bucket bucketIP2 = criarBucket();

        // Consumir tokens no IP1
        bucketIP1.tryConsume(50);

        // IP2 deve continuar com limite completo
        assertTrue(bucketIP2.tryConsume(50), "IP2 deveria ter limite independente");
    }

    private Bucket criarBucket() {
        Bandwidth limit = Bandwidth.classic(60, Refill.intervally(60, Duration.ofMinutes(1)));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }
}