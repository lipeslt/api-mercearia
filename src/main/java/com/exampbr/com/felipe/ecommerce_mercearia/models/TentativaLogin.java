package com.exampbr.com.felipe.ecommerce_mercearia.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tentativa_login")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TentativaLogin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, columnDefinition = "INTEGER DEFAULT 0")
    private Integer tentativas = 0;

    @Column(name = "ultima_tentativa")
    private LocalDateTime ultimaTentativa;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT false")
    private Boolean bloqueado = false;

    @Column(name = "data_bloqueio")
    private LocalDateTime dataBloqueio;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}