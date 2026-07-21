package com.example.veiculo.dto.ReervaVendaVeiculo;

import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.model.ReservaVendaVeiculo;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

public record VeiculoCanceladoDtoSaida(
        String status,
        Long vendaId,
        String descricao,
        String ano,
        String cor,
        String chassi,
        BigDecimal valor,
        String dtReserva,
        String dtCancelamento,
        String cliente) {

    public static VeiculoCanceladoDtoSaida ConverteDto(ReservaVendaVeiculo e) {
        return new VeiculoCanceladoDtoSaida(Libs.getNomeStatusReserva(e.getStatus()),
                e.getId(),
                e.getVeiculo().getVersao().getModelo().getMarca().getNome() + " " + e.getVeiculo().getVersao().getModelo().getNome() + " " + e.getVeiculo().getVersao().getNome(),
                e.getVeiculo().getAnoFabricacao() + " / " + e.getVeiculo().getAnoModelo(),
                e.getVeiculo().getCor().getNome(),
                e.getVeiculo().getChassi(),
                e.getValor(),
                e.getDtReserva().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                e.getDtCancelamento().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")),
                e.getCliente().getNome());
    }
}