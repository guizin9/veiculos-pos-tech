package com.example.veiculo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "username", length = 50, unique = true, nullable = false)
    private String username;

    @Column(name = "senha", length = 100, nullable = false)
    private String senha;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "usuario_role", joinColumns = @JoinColumn(name = "id_usuario"))
    @Column(name = "role", length = 15)
    private Set<Role> roles = new HashSet<>();

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    @Column(name = "id_cliente")
    private Long clienteId;

    @Column(name = "dtOpera")
    private OffsetDateTime dtOpera;

    public Usuario() {
    }
}
