package com.exampbr.com.felipe.ecommerce_mercearia.repositories;

import com.exampbr.com.felipe.ecommerce_mercearia.models.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Usuario findByEmail(String email);
}