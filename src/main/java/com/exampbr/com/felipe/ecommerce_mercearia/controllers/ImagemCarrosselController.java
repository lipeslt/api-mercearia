package com.exampbr.com.felipe.ecommerce_mercearia.controllers;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.CarrosselDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.services.ImagemCarrosselService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carrossel")
public class ImagemCarrosselController {
    private final ImagemCarrosselService service;
    public ImagemCarrosselController(ImagemCarrosselService service) {
        this.service = service;
    }
    @PostMapping
    public ResponseEntity<CarrosselDTO.Response> criar(@Valid @RequestBody CarrosselDTO.Request dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.salvar(dto));
    }
    @GetMapping
    public ResponseEntity<List<CarrosselDTO.Response>> listar() {
        return ResponseEntity.ok(service.listarOrdenado());
    }
}