package com.exampbr.com.felipe.ecommerce_mercearia.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "tb_categorias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String nome;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private java.time.OffsetDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private java.time.OffsetDateTime atualizadoEm;

    @Column(name = "criado_por")
    private UUID criadoPor;

    @Column(name = "atualizado_por")
    private UUID atualizadoPor;

    @Column(nullable = false)
    private Boolean ativo = true;

    @PrePersist
    protected void onCreate() {
        this.criadoEm = java.time.OffsetDateTime.now();
        this.atualizadoEm = java.time.OffsetDateTime.now();
        this.ativo = true;
    }

    @PreUpdate
    protected void onUpdate() {
        this.atualizadoEm = java.time.OffsetDateTime.now();
    }
}