package com.example.veiculo.repository;

import com.example.veiculo.model.ReservaVendaVeiculo;
import com.example.veiculo.model.Veiculo;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {
    Optional<Veiculo> findByChassi(String chassi);
    List<Veiculo> findByStatusOrderByValorAsc(String status, Sort sort);
    boolean existsById(Long id);
    boolean existsByIdAndValor(Long id, BigDecimal valor);
    boolean existsByChassi(String chassi);
    boolean existsByChassiAndIdNot(String chassi, Long id);
    boolean existsByCorId(Long id);
    boolean existsByVersaoId(Long id);
}
