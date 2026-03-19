package com.exampbr.com.felipe.ecommerce_mercearia.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secretKey;

    @Value("${api.security.token.expiration}")
    private long accessTokenExpiration; // 1 hora em ms

    @Value("${api.security.refresh-token.expiration:604800000}") // 7 dias por padrão
    private long refreshTokenExpiration;

    public String gerarAccessToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            return JWT.create()
                    .withIssuer("ecommerce-mercearia")
                    .withSubject(usuario.getEmail())
                    .withClaim("id", usuario.getId())
                    .withClaim("nome", usuario.getNome())
                    .withClaim("tipo", "ACCESS")
                    .withExpiresAt(gerarDataExpiracao(accessTokenExpiration))
                    .sign(algorithm);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Erro ao gerar Access Token JWT", e);
        }
    }

    public String gerarRefreshToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            return JWT.create()
                    .withIssuer("ecommerce-mercearia")
                    .withSubject(usuario.getEmail())
                    .withClaim("id", usuario.getId())
                    .withClaim("tipo", "REFRESH")
                    .withExpiresAt(gerarDataExpiracao(refreshTokenExpiration))
                    .sign(algorithm);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Erro ao gerar Refresh Token JWT", e);
        }
    }

    public String validarToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            String email = JWT.require(algorithm)
                    .withIssuer("ecommerce-mercearia")
                    .build()
                    .verify(token)
                    .getSubject();

            // Verificar se é um access token
            String tipo = JWT.require(algorithm)
                    .withIssuer("ecommerce-mercearia")
                    .build()
                    .verify(token)
                    .getClaim("tipo")
                    .asString();

            if (!"ACCESS".equals(tipo)) {
                return "";
            }

            return email;
        } catch (JWTVerificationException exception) {
            return "";
        }
    }

    public String validarRefreshToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secretKey);
            String email = JWT.require(algorithm)
                    .withIssuer("ecommerce-mercearia")
                    .build()
                    .verify(token)
                    .getSubject();

            String tipo = JWT.require(algorithm)
                    .withIssuer("ecommerce-mercearia")
                    .build()
                    .verify(token)
                    .getClaim("tipo")
                    .asString();

            if (!"REFRESH".equals(tipo)) {
                return "";
            }

            return email;
        } catch (JWTVerificationException exception) {
            return "";
        }
    }

    public String getLoginFromToken(String token) {
        return validarToken(token);
    }

    private Instant gerarDataExpiracao(long expiracaoEm) {
        return LocalDateTime.now()
                .plusNanos(expiracaoEm * 1_000_000L)
                .atZone(ZoneId.systemDefault())
                .toInstant();
    }
}