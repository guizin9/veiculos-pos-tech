package com.example.veiculo.dto.Veiculo;

import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.model.Veiculo;

import java.math.BigDecimal;

public record VeiculoAVendaDtoSaida(
        String status,
        Long veiculoId,
        String marcaNome,
        String modeloNome,
        String versaoNome,
        Short anoFabricacao,
        Short anoModelo,
        String corNome,
        String chassi,
        BigDecimal valor) {

    public static VeiculoAVendaDtoSaida ConverteDto(Veiculo e) {
        return new VeiculoAVendaDtoSaida(Libs.getNomeStatusReserva(e.getStatus()),
                e.getId(),
                e.getVersao().getModelo().getMarca().getNome(),
                e.getVersao().getModelo().getNome(),
                e.getVersao().getNome(),
                e.getAnoFabricacao(),
                e.getAnoModelo(),
                e.getCor().getNome(),
                e.getChassi(),
                e.getValor());
    }
}