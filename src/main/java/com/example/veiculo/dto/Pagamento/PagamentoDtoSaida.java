package com.example.veiculo.dto.Pagamento;

import com.example.veiculo.model.Pagamento;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PagamentoDtoSaida(
        Long id,
        String codigo,
        Long reservaId,
        BigDecimal valor,
        String status,
        OffsetDateTime dataGeracao,
        OffsetDateTime dataExpiracao,
        OffsetDateTime dataPagamento
) {
    public static PagamentoDtoSaida ConverteDto(Pagamento e) {
        return new PagamentoDtoSaida(
                e.getId(),
                e.getCodigo(),
                e.getReserva() != null ? e.getReserva().getId() : null,
                e.getValor(),
                e.getStatus() != null ? e.getStatus().name() : null,
                e.getDataGeracao(),
                e.getDataExpiracao(),
                e.getDataPagamento()
        );
    }
}
