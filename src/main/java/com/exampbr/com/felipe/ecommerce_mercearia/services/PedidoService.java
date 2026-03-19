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

@Service
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Page<Pedido> listar(Pageable pageable) {
        return pedidoRepository.findAll(pageable);
    }

    public Pedido buscarPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));
    }

    @Transactional
    public Pedido criar(PedidoRequestDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.usuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado"));

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setStatus(dto.status() != null ? dto.status() : "PENDENTE");

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido atualizar(Long id, PedidoRequestDTO dto) {
        Pedido pedido = buscarPorId(id);

        if (dto.status() != null) {
            pedido.setStatus(dto.status());
        }

        return pedidoRepository.save(pedido);
    }

    @Transactional
    public void deletar(Long id) {
        Pedido pedido = buscarPorId(id);
        pedidoRepository.delete(pedido);
    }
}