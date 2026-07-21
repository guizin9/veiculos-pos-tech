package com.example.veiculo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@Table
@Data
@EntityListeners(AuditingEntityListener.class)
public class Cor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "Nome", length = 30, unique = true, nullable = false)
    private String nome;

//    @OneToMany(mappedBy = "marca", fetch = FetchType.LAZY)
//    private List<Modelo> modelos = new ArrayList<>();

    @Column(name = "dtOpera")
    private OffsetDateTime dtOpera;

    public Cor() {
    }
}
