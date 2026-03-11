package com.exampbr.com.felipe.ecommerce_mercearia.services;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.*;
import com.exampbr.com.felipe.ecommerce_mercearia.enums.StatusPedido;
import com.exampbr.com.felipe.ecommerce_mercearia.models.ItemPedido;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Pedido;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Produto;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.PedidoRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProdutoRepository produtoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProdutoRepository produtoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.produtoRepository = produtoRepository;
    }
    @Transactional
    public PedidoResponseDTO criarPedido(PedidoRequestDTO dto) {
        Pedido pedido = new Pedido();
        pedido.setDataCriacao(Instant.now());
        pedido.setStatus(StatusPedido.AGUARDANDO_PAGAMENTO);

        BigDecimal valorTotalGeral = BigDecimal.ZERO;
        List<ItemPedido> itensModel = new ArrayList<>();

        for (ItemPedidoRequestDTO itemDto : dto.itens()) {
            // Busca o produto no banco
            Produto produto = produtoRepository.findById(itemDto.produtoId())
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado: " + itemDto.produtoId()));
            if (produto.getEstoque() < itemDto.quantidade()) {
                throw new IllegalArgumentException("Estoque insuficiente para o produto: " + produto.getNome()
                        + ". Estoque atual: " + produto.getEstoque());
            }
            produto.setEstoque(produto.getEstoque() - itemDto.quantidade());
            ItemPedido itemPedido = new ItemPedido();
            itemPedido.setProduto(produto);
            itemPedido.setPedido(pedido);
            itemPedido.setQuantidade(itemDto.quantidade());
            itemPedido.setPrecoUnitario(produto.getPreco());
            BigDecimal subTotal = itemPedido.getPrecoUnitario().multiply(new BigDecimal(itemPedido.getQuantidade()));
            valorTotalGeral = valorTotalGeral.add(subTotal);

            itensModel.add(itemPedido);
        }
        pedido.setItens(itensModel);
        pedido.setValorTotal(valorTotalGeral);
        pedido = pedidoRepository.save(pedido);
        return toDTO(pedido);
    }
    private PedidoResponseDTO toDTO(Pedido pedido) {
        List<ItemPedidoResponseDTO> itensDTO = pedido.getItens().stream().map(item -> {
            BigDecimal subTotal = item.getPrecoUnitario().multiply(new BigDecimal(item.getQuantidade()));
            return new ItemPedidoResponseDTO(
                    item.getId(),
                    item.getProduto().getNome(),
                    item.getQuantidade(),
                    item.getPrecoUnitario(),
                    subTotal
            );
        }).collect(Collectors.toList());

        return new PedidoResponseDTO(
                pedido.getId(),
                pedido.getDataCriacao(),
                pedido.getStatus(),
                pedido.getValorTotal(),
                itensDTO
        );
    }
}