package com.exampbr.com.felipe.ecommerce_mercearia.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(Usuario usuario) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        String token = JWT.create()
                .withIssuer("auth-api")
                .withSubject(usuario.getEmail())
                .withClaim("id", usuario.getId().toString())
                .withClaim("role", usuario.getRole())
                .withExpiresAt(generateExpirationDate())
                .sign(algorithm);
        return token;
    }

    public String validateToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        return JWT.require(algorithm)
                .withIssuer("auth-api")
                .build()
                .verify(token)
                .getSubject();
    }

    public UUID getUsuarioIdFromToken(String token) {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        String idString = JWT.require(algorithm)
                .withIssuer("auth-api")
                .build()
                .verify(token)
                .getClaim("id")
                .asString();
        return UUID.fromString(idString);
    }

    private Instant generateExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}