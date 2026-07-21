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
public class ReservaVendaVeiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "status", length = 1, nullable = false)
    private String status;

    @Column(name = "valor", precision = 9, scale = 2, nullable = false)
    private BigDecimal valor;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_veiculo", nullable = false)
    private Veiculo veiculo;

    @Column(name = "dtReserva")
    private OffsetDateTime dtReserva;

    @Column(name = "dtCancelamento")
    private OffsetDateTime dtCancelamento;

    @Column(name = "dtVenda")
    private OffsetDateTime dtVenda;

    @Column(name = "retirado", length = 1)
    private String retirado;

    @Column(name = "dtRetirada")
    private OffsetDateTime dtRetirada;

    @Column(name = "dtOpera")
    private OffsetDateTime dtOpera;

    public ReservaVendaVeiculo() {
    }

    public void vendaVeiculo()    { setStatus("V"); }
    public void reservaVeiculo()  { setStatus("R"); }
    public void cancelaVeiculo()  { setStatus("C"); }
    public void retiradaVeiculo() { setRetirado("S"); }
}
