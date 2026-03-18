package com.exampbr.com.felipe.ecommerce_mercearia.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class ArquivoService {

    private final Cloudinary cloudinary;

    // O Spring vai injetar essa URL direto do seu application.properties
    public ArquivoService(@Value("${api.cloudinary.url}") String cloudinaryUrl) {
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }

    public String fazerUpload(MultipartFile arquivo) {
        try {
            // Envia o arquivo para a nuvem
            Map uploadResult = cloudinary.uploader().upload(arquivo.getBytes(), ObjectUtils.emptyMap());

            // Retorna a URL segura (https) da imagem salva
            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            throw new RuntimeException("Falha ao fazer upload da imagem para a nuvem: " + e.getMessage());
        }
    }
}