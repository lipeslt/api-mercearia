package com.exampbr.com.felipe.ecommerce_mercearia.config;

import com.exampbr.com.felipe.ecommerce_mercearia.repositories.UsuarioRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.services.TokenService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityFilter.class);
    private final TokenService tokenService;
    private final UsuarioRepository usuarioRepository;

    public SecurityFilter(TokenService tokenService, UsuarioRepository usuarioRepository) {
        this.tokenService = tokenService;
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestUri = request.getRequestURI();

        try {
            if (requestUri.startsWith("/auth") ||
                    requestUri.startsWith("/swagger-ui") ||
                    requestUri.startsWith("/v3/api-docs") ||
                    requestUri.startsWith("/api-docs") ||
                    requestUri.startsWith("/webjars") ||
                    requestUri.startsWith("/actuator") ||
                    requestUri.equals("/health")) {
                logger.debug("Rota pública acessada: {}", requestUri);
                filterChain.doFilter(request, response);
                return;
            }
            var token = this.recuperarToken(request);

            if (token != null) {
                try {
                    var email = tokenService.validateToken(token);

                    if (email != null && !email.isEmpty()) {
                        UserDetails usuario = usuarioRepository.findByEmail(email);

                        if (usuario != null) {
                            var authentication = new UsernamePasswordAuthenticationToken(usuario, null, usuario.getAuthorities());
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                            logger.info("Usuário autenticado: {}", email);
                        } else {
                            logger.warn("Usuário não encontrado no banco: {}", email);
                        }
                    } else {
                        logger.warn("Token inválido, vazio ou expirado");
                    }
                } catch (JWTVerificationException exception) {
                    logger.error("Token JWT inválido: {}", exception.getMessage());
                }
            }
            filterChain.doFilter(request, response);

        } catch (Exception exception) {
            logger.error("Erro inesperado no SecurityFilter: {}", exception.getMessage(), exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Erro ao processar requisição\"}");
        }
    }

    private String recuperarToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null) return null;
        return authHeader.replace("Bearer ", "");
    }
}