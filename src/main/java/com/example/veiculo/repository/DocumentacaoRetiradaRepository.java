package com.example.veiculo.repository;

import com.example.veiculo.model.DocumentacaoRetirada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DocumentacaoRetiradaRepository extends JpaRepository<DocumentacaoRetirada, Long> {
    Optional<DocumentacaoRetirada> findByReservaId(Long reservaId);
    boolean existsByReservaId(Long reservaId);
}
