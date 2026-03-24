package com.exampbr.com.felipe.ecommerce_mercearia.services;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.PedidoRequestDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Pedido;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Usuario;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.PedidoRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

        return pedidoRepository.save(pedido);
    }

    public Optional<Pedido> obterPedidoPorId(UUID id) {
        return pedidoRepository.findById(id);
    }

    public List<Pedido> obterPedidosPorUsuario(UUID usuarioId) {
        return pedidoRepository.findByUsuarioId(usuarioId);
    }

    public void deletarPedido(UUID id) {
        pedidoRepository.deleteById(id);
    }
}