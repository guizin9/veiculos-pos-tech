package com.example.veiculo.dto.DocumentacaoRetirada;

import com.example.veiculo.model.DocumentacaoRetirada;

import java.time.OffsetDateTime;

public record DocumentacaoRetiradaDtoSaida(
        Long id,
        Long reservaId,
        Long clienteId,
        Long veiculoId,
        String numeroDocumento,
        OffsetDateTime dataEmissao,
        OffsetDateTime dataRetirada,
        String status
) {
    public static DocumentacaoRetiradaDtoSaida ConverteDto(DocumentacaoRetirada e) {
        return new DocumentacaoRetiradaDtoSaida(
                e.getId(),
                e.getReservaId(),
                e.getClienteId(),
                e.getVeiculoId(),
                e.getNumeroDocumento(),
                e.getDataEmissao(),
                e.getDataRetirada(),
                e.getStatus() != null ? e.getStatus().name() : null
        );
    }
}
