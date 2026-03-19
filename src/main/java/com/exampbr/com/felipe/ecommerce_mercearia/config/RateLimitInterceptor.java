package com.exampbr.com.felipe.ecommerce_mercearia.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = obterIpRequisicao(request);
        String rota = request.getRequestURI();

        // Rotas excluídas do rate limit
        if (rotaExcluida(rota)) {
            return true;
        }

        Bucket bucket = obterOuCriarBucket(ip);

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.getWriter().write("""
                {
                    "timestamp": "%s",
                    "status": 429,
                    "erro": "Limite de requisições excedido",
                    "mensagem": "Você excedeu o limite de requisições. Tente novamente em 1 minuto.",
                    "caminho": "%s"
                }
                """.formatted(java.time.Instant.now(), rota));
            return false;
        }
    }

    private Bucket obterOuCriarBucket(String ip) {
        return buckets.computeIfAbsent(ip, k -> {
            // Limite: 100 requisições por minuto
            Bandwidth limit = Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1)));
            return Bucket4j.builder()
                    .addLimit(limit)
                    .build();
        });
    }

    private String obterIpRequisicao(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip.split(",")[0].trim();
    }

    private boolean rotaExcluida(String rota) {
        return rota.startsWith("/swagger-ui") ||
                rota.startsWith("/v3/api-docs") ||
                rota.startsWith("/actuator") ||
                rota.equals("/health");
    }
}