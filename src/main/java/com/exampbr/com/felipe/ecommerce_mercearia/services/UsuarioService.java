package com.exampbr.com.felipe.ecommerce_mercearia.services;

import com.exampbr.com.felipe.ecommerce_mercearia.models.Usuario;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.UsuarioRepository;
import com.exampbr.com.felipe.ecommerce_mercearia.repositories.TentativaLoginRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TentativaLoginRepository tentativaLoginRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    public Usuario findByEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
    public Optional<Usuario> findById(Long id) {
        return usuarioRepository.findById(id);
    }
    public Usuario save(Usuario usuario) {
        if (usuario.getSenha() != null && !usuario.getSenha().isEmpty()) {
            usuario.setSenha(passwordEncoder.encode(usuario.getSenha()));
        }
        return usuarioRepository.save(usuario);
    }
    public Usuario update(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
    public void deleteById(Long id) {
        usuarioRepository.deleteById(id);
    }
    public boolean validarSenha(Usuario usuario, String senhaPlana) {
        if (usuario == null || senhaPlana == null) {
            return false;
        }
        return passwordEncoder.matches(senhaPlana, usuario.getSenha());
    }
    @Transactional
    public void registrarTentativaFalha(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email);

        if (usuario != null) {
        }
    }
    @Transactional
    public void limparTentativas(String email) {
    }
}