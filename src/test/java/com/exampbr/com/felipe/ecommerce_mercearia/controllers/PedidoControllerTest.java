package com.exampbr.com.felipe.ecommerce_mercearia.controllers;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.PedidoDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.entities.Pedido;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.PedidoRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.services.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Testes para PedidoController
 * Valida CRUD e fluxo de pedidos
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PedidoController - Testes de Pedidos")
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoService pedidoService;

    @MockBean
    private PedidoRepository pedidoRepository;

    private Pedido pedidoTeste;
    private PedidoDTO pedidoDTOTeste;

    @BeforeEach
    void setup() {
        // Setup pedido de teste
        pedidoTeste = new Pedido();
        pedidoTeste.setId(1L);
        pedidoTeste.setNumero("PED-2026-001");
        pedidoTeste.setTotal(new BigDecimal("150.00"));
        pedidoTeste.setStatus("PENDENTE");
        pedidoTeste.setCriadoEm(LocalDateTime.now());

        // Setup DTO
        pedidoDTOTeste = new PedidoDTO();
        pedidoDTOTeste.setEndereco("Rua Test 123");
        pedidoDTOTeste.setTelefone("11999999999");
    }

    @Test
    @DisplayName("✅ Deve listar pedidos do usuário autenticado")
    @WithMockUser
    void testListarMeusPedidos() throws Exception {
        List<Pedido> pedidos = Arrays.asList(pedidoTeste);
        when(pedidoRepository.findAll()).thenReturn(pedidos);

        mockMvc.perform(get("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("✅ Deve obter pedido por ID")
    @WithMockUser
    void testObtenerPedidoPorId() throws Exception {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedidoTeste));

        mockMvc.perform(get("/api/pedidos/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.numero").value("PED-2026-001"));
    }

    @Test
    @DisplayName("❌ Deve retornar 404 para pedido inexistente")
    @WithMockUser
    void testObtenerPedidoInexistente() throws Exception {
        when(pedidoRepository.findById(anyLong())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/pedidos/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("✅ Deve criar novo pedido")
    @WithMockUser
    void testCriarPedido() throws Exception {
        when(pedidoService.criarPedido(any(PedidoDTO.class)))
                .thenReturn(pedidoTeste);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoDTOTeste)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.numero").value("PED-2026-001"));
    }

    @Test
    @DisplayName("❌ Deve rejeitar criação de pedido sem autenticação")
    void testCriarPedidoSemAutenticacao() throws Exception {
        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoDTOTeste)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("✅ Deve atualizar status do pedido (ADMIN)")
    @WithMockUser(roles = "ADMIN")
    void testAtualizarStatusPedido() throws Exception {
        pedidoTeste.setStatus("ENVIADO");
        when(pedidoService.atualizarStatusPedido(anyLong(), any(String.class)))
                .thenReturn(pedidoTeste);

        mockMvc.perform(put("/api/pedidos/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ENVIADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENVIADO"));
    }

    @Test
    @DisplayName("❌ Deve rejeitar atualização de status sem ADMIN")
    @WithMockUser(roles = "CLIENTE")
    void testAtualizarStatusSemPermissao() throws Exception {
        mockMvc.perform(put("/api/pedidos/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"ENVIADO\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("✅ Deve cancelar pedido com status PENDENTE")
    @WithMockUser
    void testCancelarPedidoPendente() throws Exception {
        pedidoTeste.setStatus("CANCELADO");
        when(pedidoService.cancelarPedido(anyLong()))
                .thenReturn(pedidoTeste);

        mockMvc.perform(put("/api/pedidos/1/cancelar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADO"));
    }

    @Test
    @DisplayName("❌ Deve impedir cancelamento de pedido enviado")
    @WithMockUser
    void testCancelarPedidoEnviado() throws Exception {
        pedidoTeste.setStatus("ENVIADO");
        when(pedidoService.cancelarPedido(anyLong()))
                .thenThrow(new IllegalStateException("Não é possível cancelar pedido enviado"));

        mockMvc.perform(put("/api/pedidos/1/cancelar")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("✅ Deve validar endereço obrigatório")
    void testCriarPedidoSemEndereco() throws Exception {
        pedidoDTOTeste.setEndereco("");

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoDTOTeste)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("✅ Deve validar telefone para contato")
    void testCriarPedidoComTelefoneInvalido() throws Exception {
        pedidoDTOTeste.setTelefone("123");  // Muito curto

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedidoDTOTeste)))
                .andExpect(status().isBadRequest());
    }
}