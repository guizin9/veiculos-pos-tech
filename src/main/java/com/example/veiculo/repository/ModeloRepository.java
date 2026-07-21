package com.example.veiculo.repository;

import com.example.veiculo.model.Modelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface ModeloRepository extends JpaRepository<Modelo, Long> {
    Optional<Modelo> findByNome(String Nome);
    boolean existsById(Long id);
    boolean existsByNome(String nome);
    boolean existsByNomeAndIdNot(String nome, Long id);
    boolean existsByMarcaId(Long id);
}
