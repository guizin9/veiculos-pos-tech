package com.example.veiculo.dto.ReervaVendaVeiculo;

import java.math.BigDecimal;

public record VeiculoMarcaDtoSaida(String marca, Long qtdeDisponivel, Long qtdeReservada, Long qtdeVendida, Long qtdeTotal) {
    public static VeiculoMarcaDtoSaida ConverteDto(VeiculoMarcaDtoSaida e) {
        return new VeiculoMarcaDtoSaida(e.marca(), e.qtdeDisponivel(), e.qtdeReservada(), e.qtdeVendida(), e.qtdeTotal());
    }
}