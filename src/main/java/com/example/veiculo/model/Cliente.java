package com.example.veiculo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@Table
@Data
@EntityListeners(AuditingEntityListener.class)
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome", length = 70, unique = true, nullable = false)
    private String nome;

    @Column(name = "cpf", length = 15, unique = true)
    private String cpf;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "logradouro", length = 50)
    private String logradouro;
    @Column(name = "numero", length = 10)
    private String numero;
    @Column(name = "complemento", length = 20)
    private String complemento;
    @Column(name = "bairro", length = 30)
    private String bairro;
    @Column(name = "cidade", length = 40)
    private String cidade;
    @Column(name = "estado", length = 3)
    private String estado;
    @Column(name = "cep", length = 10)
    private String cep;
    @Column(name = "celular", length = 15)
    private String celular;
    @Column(name = "foneFixo", length = 15)
    private String foneFixo;
    @Column(name = "email", length = 50)
    private String email;
    @Column(name = "dtOpera")
    private OffsetDateTime dtOpera;
    public Cliente() {
    }
}
