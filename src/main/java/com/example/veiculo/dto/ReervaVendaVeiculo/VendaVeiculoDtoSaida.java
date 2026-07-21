package com.example.veiculo.dto.ReervaVendaVeiculo;

import com.example.veiculo.model.ReservaVendaVeiculo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record VendaVeiculoDtoSaida(
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
        OffsetDateTime dtVenda,
        OffsetDateTime dtOpera) {

    public static VendaVeiculoDtoSaida ConverteDto(ReservaVendaVeiculo e) {
        return new VendaVeiculoDtoSaida(
                e.getId(), e.getStatus(),
                e.getValor(),
                e.getVeiculo().getVersao().getModelo().getMarca().getNome(),
                e.getVeiculo().getVersao().getModelo().getNome(),
                e.getVeiculo().getVersao().getNome(),
                e.getVeiculo().getId(),
                e.getCliente().getId(),
                e.getCliente().getNome(),
                e.getDtReserva(),
                e.getDtVenda(),
                e.getDtOpera());
    }
}