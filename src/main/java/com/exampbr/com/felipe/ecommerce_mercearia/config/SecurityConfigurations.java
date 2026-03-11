package com.exampbr.com.felipe.ecommerce_mercearia.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfigurations {

    private final SecurityFilter securityFilter;

    public SecurityConfigurations(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable) // Desabilitado pois o JWT já protege contra ataques CSRF
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // API REST não guarda sessão
                .authorizeHttpRequests(authorize -> authorize
                                // Rotas de Autenticação (Públicas)
                                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/auth/registrar").permitAll()

// Rotas de Leitura (Públicas para ver o catálogo e carrossel)
                                .requestMatchers(HttpMethod.GET, "/api/produtos", "/api/produtos/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/categorias", "/api/categorias/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/carrossel", "/api/carrossel/**").permitAll()

// Rotas exclusivas do Administrador
                                .requestMatchers(HttpMethod.POST, "/api/produtos").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/categorias").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/carrossel").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/api/dashboard/**").hasRole("ADMIN")

// Qualquer outra requisição exige apenas estar logado
                                .anyRequest().authenticated()
                )
                // Coloca o nosso filtro de JWT antes do filtro padrão do Spring
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    // Bean para injetar o AuthenticationManager no nosso controller de login futuramente
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // Bean para o algoritmo de hash de senhas (Criptografia forte)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}