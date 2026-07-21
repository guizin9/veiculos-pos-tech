package com.example.veiculo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table
@Data
@EntityListeners(AuditingEntityListener.class)
public class Veiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "status", length = 1, nullable = false)
    private String status;

    @Column(name = "ano_fabricacao", length = 4, nullable = false)
    private Short anoFabricacao;

    @Column(name = "ano_modelo", length = 4, nullable = false)
    private Short anoModelo;

    @Column(name = "chassi", length = 17, nullable = false)
    private String chassi;

    @Column(name = "valor", precision = 9, scale = 2, nullable = false)
    private BigDecimal valor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_versao", nullable = false)
    private Versao versao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cor", nullable = false)
    private Cor cor;

    @Column(name = "dtOpera")
    private OffsetDateTime dtOpera;

    public Veiculo() {
    }

    public void vendaVeiculo()    { setStatus("V"); }
    public void ativaVeiculo()    { setStatus("A"); }
    public void reservaVeiculo()  { setStatus("R"); }
    public void desativaVeiculo() { setStatus("D"); }
}
