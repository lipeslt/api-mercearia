package com.exampbr.com.felipe.ecommerce_mercearia.repositories;

import com.exampbr.com.felipe.ecommerce_mercearia.models.TentativaLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TentativaLoginRepository extends JpaRepository<TentativaLogin, Long> {
    Optional<TentativaLogin> findByEmail(String email);
}