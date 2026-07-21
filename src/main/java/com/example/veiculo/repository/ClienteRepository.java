package com.example.veiculo.repository;

import com.example.veiculo.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByNome(String Nome);
    boolean existsById(Long id);
    boolean existsByNome(String nome);
    boolean existsByCpf(String cpf);
    boolean existsByNomeAndIdNot(String nome, Long id);
    boolean existsByCpfAndIdNot(String cpf, Long id);
    boolean existsByIdAndDtOpera(Long id, OffsetDateTime dtOpera);
}
