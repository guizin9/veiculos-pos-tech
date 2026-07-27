package com.example.veiculo.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;

@Entity
@Table(name = "documentacao_retirada")
@Data
@EntityListeners(AuditingEntityListener.class)
public class DocumentacaoRetirada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "id_reserva", nullable = false)
    private Long reservaId;

    @Column(name = "id_cliente", nullable = false)
    private Long clienteId;

    @Column(name = "id_veiculo", nullable = false)
    private Long veiculoId;

    @Column(name = "numero_documento", length = 40, nullable = false, unique = true)
    private String numeroDocumento;

    @Column(name = "data_emissao", nullable = false)
    private OffsetDateTime dataEmissao;

    @Column(name = "data_retirada")
    private OffsetDateTime dataRetirada;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 15, nullable = false)
    private StatusDocumentacao status;

    public DocumentacaoRetirada() {
    }
}
