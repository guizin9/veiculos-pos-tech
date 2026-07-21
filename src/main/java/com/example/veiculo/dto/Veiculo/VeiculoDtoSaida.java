package com.example.veiculo.dto.Veiculo;

import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.model.Veiculo;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record VeiculoDtoSaida(
        String status,
        Long veiculoId,
        Short anoFabricacao,
        Short anoModelo,
        String chassi,
        BigDecimal valor,
        Long versaoId,
        String versaoNome,
        Long corId,
        String corNome,
        OffsetDateTime dtOpera) {

    public static VeiculoDtoSaida ConverteDto(Veiculo e) {
        return new VeiculoDtoSaida(
                Libs.getNomeStatusReserva(e.getStatus()),
                e.getId(),
                e.getAnoFabricacao(),
                e.getAnoModelo(),
                e.getChassi(),
                e.getValor(),
                e.getVersao().getId(),
                e.getVersao().getNome(),
                e.getCor().getId(),
                e.getCor().getNome(),
                e.getDtOpera());
    }
}