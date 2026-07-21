package com.example.veiculo.dto.ReervaVendaVeiculo;

import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.model.ReservaVendaVeiculo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReservaVendaVeiculoDtoSaida(
        Long reservaId,
        String status,
        BigDecimal valor,
        String marcaNome,
        String modeloNome,
        String versaoNome,
        Long veiculoId,
        Long clienteId,
        String clienteNome,
        OffsetDateTime dtReserva,
        OffsetDateTime dtCancelamento,
        OffsetDateTime dtVenda,
        OffsetDateTime dtOpera) {

    public static ReservaVendaVeiculoDtoSaida ConverteDto(ReservaVendaVeiculo e) {
        return new ReservaVendaVeiculoDtoSaida(
                e.getId(),
                Libs.getNomeStatusReserva(e.getStatus()),
                e.getValor(),
                e.getVeiculo().getVersao().getModelo().getMarca().getNome(),
                e.getVeiculo().getVersao().getModelo().getNome(),
                e.getVeiculo().getVersao().getNome(),
                e.getVeiculo().getId(),
                e.getCliente().getId(),
                e.getCliente().getNome(),
                e.getDtReserva(),
                e.getDtCancelamento(),
                e.getDtVenda(),
                e.getDtOpera());
    }
}