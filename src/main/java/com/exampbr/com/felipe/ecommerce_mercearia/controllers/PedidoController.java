package com.exampbr.com.felipe.ecommerce_mercearia.controllers;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.PedidoRequestDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Pedido;
import com.exampbr.com.felipe.ecommerce_mercearia.services.PedidoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/pedidos")
@Tag(name = "Pedidos", description = "Endpoints para gerenciar pedidos")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @PostMapping
    @Operation(summary = "Criar novo pedido")
    public ResponseEntity<Pedido> criarPedido(@Valid @RequestBody PedidoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pedidoService.criarPedido(dto));
    }

    @GetMapping
    @Operation(summary = "Listar todos os pedidos (Admin)")
    public ResponseEntity<Page<Pedido>> listarTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "dataCriacao") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(pedidoService.listarTodos(pageable, status));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obter pedido por ID")
    public ResponseEntity<Pedido> obterPedido(@PathVariable UUID id) {
        Optional<Pedido> pedido = pedidoService.obterPedidoPorId(id);
        return pedido.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(summary = "Listar pedidos de um usuário")
    public ResponseEntity<List<Pedido>> obterPedidosUsuario(@PathVariable UUID usuarioId) {
        List<Pedido> pedidos = pedidoService.obterPedidosPorUsuario(usuarioId);
        return ResponseEntity.ok(pedidos);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Atualizar status de um pedido (Admin)")
    public ResponseEntity<Pedido> atualizarStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(pedidoService.atualizarStatus(id, body.get("status")));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Deletar pedido")
    public ResponseEntity<Void> deletarPedido(@PathVariable UUID id) {
        pedidoService.deletarPedido(id);
        return ResponseEntity.noContent().build();
    }
}
