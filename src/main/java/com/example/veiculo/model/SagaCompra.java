package com.example.veiculo.model;

import com.example.veiculo.saga.SagaEtapa;
import com.example.veiculo.saga.SagaStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.OffsetDateTime;

@Entity
@Table(name = "saga_compra", uniqueConstraints = @UniqueConstraint(columnNames = "id_reserva"))
@Data
public class SagaCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "id_reserva", nullable = false, unique = true)
    private Long reservaId;

    @Enumerated(EnumType.STRING)
    @Column(name = "etapa", length = 25, nullable = false)
    private SagaEtapa etapa;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    private SagaStatus status;

    @Column(name = "dt_operacao", nullable = false)
    private OffsetDateTime dtOperacao;

    public SagaCompra() {
    }
}
