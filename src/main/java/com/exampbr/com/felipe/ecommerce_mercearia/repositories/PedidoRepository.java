package com.exampbr.com.felipe.ecommerce_mercearia.repositories;

import com.exampbr.com.felipe.ecommerce_mercearia.models.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {
    List<Pedido> findByUsuarioId(UUID usuarioId);
    Page<Pedido> findByStatus(Pedido.StatusPedido status, Pageable pageable);
}
