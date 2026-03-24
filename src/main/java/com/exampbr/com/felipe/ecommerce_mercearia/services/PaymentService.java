package com.exampbr.com.felipe.ecommerce_mercearia.services;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.PaymentRequestDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Payment;
import com.exampbr.com.felipe.ecommerce_mercearia.models.Pedido;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.PaymentRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.PedidoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PedidoRepository pedidoRepository;

    public Page<Payment> listar(Pageable pageable) {
        return paymentRepository.findAll(pageable);
    }

    public Payment buscarPorId(UUID id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pagamento não encontrado"));
    }

    @Transactional
    public Payment criar(PaymentRequestDTO dto) {
        Pedido pedido = pedidoRepository.findById(dto.pedidoId())
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado"));

        Payment payment = new Payment();
        payment.setPedido(pedido);
        payment.setStatus("PENDENTE");

        return paymentRepository.save(payment);
    }

    @Transactional
    public Payment atualizar(UUID id, PaymentRequestDTO dto) {
        Payment payment = buscarPorId(id);

        if (dto.status() != null) {
            payment.setStatus(dto.status());
        }

        return paymentRepository.save(payment);
    }

    @Transactional
    public void deletar(UUID id) {
        Payment payment = buscarPorId(id);
        paymentRepository.delete(payment);
    }
}