package com.exampbr.com.felipe.ecommerce_mercearia.repositories;

import com.exampbr.com.felipe.ecommerce_mercearia.models.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    // Soma o valor total de todos os pedidos que NÃO foram cancelados
    @Query("SELECT SUM(p.valorTotal) FROM Pedido p WHERE p.status != 'CANCELADO'")
    BigDecimal calcularFaturamentoTotal();

    // Conta a quantidade de pedidos válidos
    @Query("SELECT COUNT(p) FROM Pedido p WHERE p.status != 'CANCELADO'")
    Long contarPedidosValidos();
}