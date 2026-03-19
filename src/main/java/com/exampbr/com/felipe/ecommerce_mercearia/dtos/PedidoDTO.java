package com.exampbr.com.felipe.ecommerce_mercearia.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public class PedidoDTO {

    private Long id;

    @NotNull(message = "Usuário é obrigatório")
    private Long usuarioId;

    @NotNull(message = "Itens do pedido são obrigatórios")
    private List<PedidoItemDTO> itens;

    private String status;

    public PedidoDTO() {}

    public PedidoDTO(Long usuarioId, List<PedidoItemDTO> itens) {
        this.usuarioId = usuarioId;
        this.itens = itens;
        this.status = "PENDENTE";
    }

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUsuarioId() { return usuarioId; }
    public void setUsuarioId(Long usuarioId) { this.usuarioId = usuarioId; }

    public List<PedidoItemDTO> getItens() { return itens; }
    public void setItens(List<PedidoItemDTO> itens) { this.itens = itens; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public static class PedidoItemDTO {
        private Long produtoId;
        private Integer quantidade;

        public PedidoItemDTO() {}

        public PedidoItemDTO(Long produtoId, Integer quantidade) {
            this.produtoId = produtoId;
            this.quantidade = quantidade;
        }

        public Long getProdutoId() { return produtoId; }
        public void setProdutoId(Long produtoId) { this.produtoId = produtoId; }

        public Integer getQuantidade() { return quantidade; }
        public void setQuantidade(Integer quantidade) { this.quantidade = quantidade; }
    }
}