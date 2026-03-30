package com.exampbr.com.felipe.ecommerce_mercearia.repositories;

import com.exampbr.com.felipe.ecommerce_mercearia.models.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, UUID> {

    @Query("SELECT DISTINCT p FROM Pedido p LEFT JOIN FETCH p.usuario LEFT JOIN FETCH p.itens WHERE p.usuario.id = :usuarioId")
    List<Pedido> findByUsuarioId(UUID usuarioId);

    @EntityGraph(attributePaths = {"usuario", "itens"})
    Optional<Pedido> findWithDetailsById(UUID id);

    Page<Pedido> findByStatus(Pedido.StatusPedido status, Pageable pageable);
}
