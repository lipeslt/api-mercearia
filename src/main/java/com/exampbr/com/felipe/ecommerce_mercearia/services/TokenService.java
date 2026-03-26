package com.exampbr.com.felipe.ecommerce_mercearia.services;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Usuario;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class TokenService {

    @Value("${api.security.token.secret}")
    private String secret;

    public String generateToken(Usuario usuario) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withIssuer("api-mercearia")
                    .withSubject(usuario.getEmail())
                    .withClaim("id", usuario.getId().toString())
                    .withClaim("nome", usuario.getNome())
                    .withClaim("email", usuario.getEmail())
                    .withClaim("role", usuario.getRole())
                    .withExpiresAt(Instant.now().plus(2, ChronoUnit.HOURS))
                    .sign(algorithm);
        } catch (JWTCreationException exception) {
            throw new RuntimeException("Error while creating token", exception);
        }
    }

    public String validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.require(algorithm)
                    .withIssuer("api-mercearia")
                    .build()
                    .verify(token)
                    .getSubject();
        } catch (JWTVerificationException exception) {
            throw new RuntimeException("Invalid token", exception);
        }
    }
}
