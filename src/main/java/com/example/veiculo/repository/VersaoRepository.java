package com.example.veiculo.repository;

import com.example.veiculo.model.Versao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VersaoRepository extends JpaRepository<Versao, Long> {
    Optional<Versao> findByNome(String Nome);
    boolean existsById(Long id);
    boolean existsByNome(String nome);
    boolean existsByNomeAndIdNot(String nome, Long id);
    boolean existsByModeloId(Long id);
}
