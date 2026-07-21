package com.example.veiculo.repository;

import com.example.veiculo.model.Cor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CorRepository extends JpaRepository<Cor, Long> {
    Optional<Cor> findByNome(String Nome);
    boolean existsById(Long id);
    boolean existsByNome(String nome);
    boolean existsByNomeAndIdNot(String nome, Long id);
}
