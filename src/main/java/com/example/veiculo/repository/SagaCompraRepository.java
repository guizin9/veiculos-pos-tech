package com.example.veiculo.repository;

import com.example.veiculo.model.SagaCompra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SagaCompraRepository extends JpaRepository<SagaCompra, Long> {
    Optional<SagaCompra> findByReservaId(Long reservaId);
}
