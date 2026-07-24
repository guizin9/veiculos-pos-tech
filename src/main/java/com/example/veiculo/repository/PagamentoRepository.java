package com.example.veiculo.repository;

import com.example.veiculo.model.Pagamento;
import com.example.veiculo.model.StatusPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    Optional<Pagamento> findByCodigo(String codigo);
    List<Pagamento> findByReservaId(Long reservaId);
    Optional<Pagamento> findFirstByReservaIdAndStatus(Long reservaId, StatusPagamento status);
    boolean existsByReservaIdAndStatus(Long reservaId, StatusPagamento status);
    List<Pagamento> findByStatusAndDataExpiracaoBefore(StatusPagamento status, OffsetDateTime limite);
}
