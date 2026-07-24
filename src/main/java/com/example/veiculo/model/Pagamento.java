package com.example.veiculo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "pagamento")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "codigo", length = 40, nullable = false, unique = true)
    private String codigo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_reserva", nullable = false)
    private ReservaVendaVeiculo reserva;

    @Column(name = "valor", precision = 9, scale = 2, nullable = false)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    private StatusPagamento status;

    @Column(name = "data_geracao", nullable = false)
    private OffsetDateTime dataGeracao;

    @Column(name = "data_expiracao", nullable = false)
    private OffsetDateTime dataExpiracao;

    @Column(name = "data_pagamento")
    private OffsetDateTime dataPagamento;

    public Pagamento() {
    }

    public void pago()      { setStatus(StatusPagamento.PAGO); }
    public void expirado()  { setStatus(StatusPagamento.EXPIRADO); }
    public void cancelado() { setStatus(StatusPagamento.CANCELADO); }

    public boolean estaPendente()  { return StatusPagamento.PENDENTE.equals(status); }
    public boolean estaVencido()   { return dataExpiracao != null && dataExpiracao.isBefore(OffsetDateTime.now()); }
}
