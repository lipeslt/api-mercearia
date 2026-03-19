package com.exampbr.com.felipe.ecommerce_mercearia.exceptions;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<StandardError> entityNotFound(EntityNotFoundException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        logger.warn("Recurso não encontrado: {}", e.getMessage());
        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Recurso não encontrado",
                e.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<StandardError> validationError(MethodArgumentNotValidException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        List<String> errosDeValidacao = e.getBindingResult().getFieldErrors().stream()
                .map(erro -> erro.getField() + ": " + erro.getDefaultMessage())
                .collect(Collectors.toList());

        logger.warn("Erro de validação: {}", errosDeValidacao);
        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Erro de validação de dados",
                "Um ou mais campos estão inválidos. Verifique a lista de erros.",
                request.getRequestURI(),
                errosDeValidacao
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<StandardError> badRequest(IllegalArgumentException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        logger.warn("Erro de regra de negócio: {}", e.getMessage());
        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Erro de Regra de Negócio",
                e.getMessage(),
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(JWTVerificationException.class)
    public ResponseEntity<StandardError> jwtVerificationError(JWTVerificationException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        logger.warn("Erro de autenticação JWT: {}", e.getMessage());
        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Erro de Autenticação",
                "Token JWT inválido, expirado ou não fornecido",
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<StandardError> accessDenied(AccessDeniedException e, HttpServletRequest request) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        logger.warn("Acesso negado: {}", e.getMessage());
        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Acesso Negado",
                "Você não possui permissão para acessar este recurso",
                request.getRequestURI(),
                null
        );
        return ResponseEntity.status(status).body(err);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> genericException(Exception e, HttpServletRequest request) {
        String requestUri = request.getRequestURI();

        // NÃO CAPTURAR ROTAS DO ACTUATOR - deixar passar
        if (requestUri.startsWith("/actuator")) {
            logger.warn("Exceção no actuator, deixando passar: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        logger.error("Erro genérico não tratado: {}", e.getMessage(), e);
        StandardError err = new StandardError(
                Instant.now(),
                status.value(),
                "Erro Interno do Servidor",
                "Ocorreu um erro inesperado. Por favor, tente novamente mais tarde.",
                requestUri,
                null
        );
        return ResponseEntity.status(status).body(err);
    }
}