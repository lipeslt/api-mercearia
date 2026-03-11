package com.exampbr.com.felipe.ecommerce_mercearia.services;

import com.exampbr.com.felipe.ecommerce_mercearia.dtos.CarrosselDTO;
import com.exampbr.com.felipe.ecommerce_mercearia.models.ImagemCarrossel;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.ImagemCarrosselRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ImagemCarrosselService {

    private final ImagemCarrosselRepository repository;

    public ImagemCarrosselService(ImagemCarrosselRepository repository) {
        this.repository = repository;
    }

    public CarrosselDTO.Response salvar(CarrosselDTO.Request dto) {
        ImagemCarrossel imagem = new ImagemCarrossel();
        imagem.setImageUrl(dto.imageUrl());
        imagem.setTituloOpcional(dto.tituloOpcional());
        imagem.setOrdemExibicao(dto.ordemExibicao());

        imagem = repository.save(imagem);
        return new CarrosselDTO.Response(imagem.getId(), imagem.getImageUrl(), imagem.getTituloOpcional(), imagem.getOrdemExibicao());
    }

    public List<CarrosselDTO.Response> listarOrdenado() {
        return repository.findAllByOrderByOrdemExibicaoAsc().stream()
                .map(img -> new CarrosselDTO.Response(img.getId(), img.getImageUrl(), img.getTituloOpcional(), img.getOrdemExibicao()))
                .collect(Collectors.toList());
    }
}