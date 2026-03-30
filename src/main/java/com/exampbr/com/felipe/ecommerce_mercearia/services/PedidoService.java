package com.exampbr.com.felipe.ecommerce_mercearia.services;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.PedidoRequestDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Pedido;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Usuario;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.PedidoRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Pedido criarPedido(PedidoRequestDTO dto) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(dto.usuarioId());
        if (usuarioOpt.isEmpty()) {
            throw new IllegalArgumentException("Usuário não encontrado");
        }
        Pedido pedido = new Pedido();
        pedido.setUsuario(usuarioOpt.get());
        pedido.setDataCriacao(OffsetDateTime.now());
        pedido.setStatus(Pedido.StatusPedido.AGUARDANDO_PAGAMENTO);
        pedido.setValorTotal(dto.valorTotal() != null ? dto.valorTotal() : BigDecimal.ZERO);
        return pedidoRepository.save(pedido);
    }

    @Transactional(readOnly = true)
    public Page<Pedido> listarTodos(Pageable pageable, String status) {
        if (status != null && !status.isBlank()) {
            Pedido.StatusPedido statusEnum = Pedido.StatusPedido.valueOf(status.toUpperCase());
            return pedidoRepository.findByStatus(statusEnum, pageable);
        }
        return pedidoRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Optional<Pedido> obterPedidoPorId(UUID id) {
        return pedidoRepository.findWithDetailsById(id);
    }

    @Transactional(readOnly = true)
    public List<Pedido> obterPedidosPorUsuario(UUID usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    public Pedido atualizarStatus(UUID id, String novoStatus) {
        Pedido pedido = pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));
        pedido.setStatus(Pedido.StatusPedido.valueOf(novoStatus.toUpperCase()));
        return pedidoRepository.save(pedido);
    }

    public void deletarPedido(UUID id) {
        pedidoRepository.deleteById(id);
    }
}
