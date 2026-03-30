package com.exampbr.com.felipe.ecommerce_mercearia.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "tb_pedidos")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pedido extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private java.util.UUID id;

    @Column(name = "data_criacao", nullable = false)
    private OffsetDateTime dataCriacao;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusPedido status;

    @Column(name = "valor_total", precision = 10, scale = 2, nullable = false)
    private BigDecimal valorTotal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"senha", "authorities", "accountNonExpired", "accountNonLocked", "credentialsNonExpired", "enabled", "username", "password"})
    private Usuario usuario;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnoreProperties("pedido")
    private List<ItemPedido> itens;

    public enum StatusPedido {
        AGUARDANDO_PAGAMENTO,
        PAGO,
        SEPARACAO_ESTOQUE,
        ENVIADO,
        ENTREGUE,
        CANCELADO
    }
}