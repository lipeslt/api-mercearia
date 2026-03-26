package com.exampbr.com.felipe.ecommerce_mercearia.controllers;

import com.exampbr.com.felipe.ecommerce_mercearia.models.Usuario;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.UsuarioRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuários", description = "Endpoints para gerenciamento de usuários (Admin)")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    @Operation(summary = "Listar todos os usuários (Admin)")
    public ResponseEntity<Page<Usuario>> listar(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") Sort.Direction direction) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        return ResponseEntity.ok(usuarioRepository.findAll(pageable));
    }

    @PutMapping("/{id}/role")
    @Operation(summary = "Atualizar role de um usuário (Admin)")
    public ResponseEntity<Usuario> atualizarRole(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    String novaRole = body.get("role");
                    if (novaRole == null || (!novaRole.equals("ADMIN") && !novaRole.equals("USER"))) {
                        throw new IllegalArgumentException("Role inválida. Use ADMIN ou USER.");
                    }
                    usuario.setRole(novaRole);
                    return ResponseEntity.ok(usuarioRepository.save(usuario));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
