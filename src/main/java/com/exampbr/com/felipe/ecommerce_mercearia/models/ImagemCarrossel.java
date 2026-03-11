package com.exampbr.com.felipe.ecommerce_mercearia.models;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "tb_carrossel")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class ImagemCarrossel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(length = 100)
    private String tituloOpcional;

    @Column(nullable = false)
    private Integer ordemExibicao; // Ex: 1, 2, 3... define quem aparece primeiro
}