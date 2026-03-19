package com.exampbr.com.felipe.ecommerce_mercearia.repositories;

import com.exampbr.com.felipe.ecommerce_mercearia.models.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
}