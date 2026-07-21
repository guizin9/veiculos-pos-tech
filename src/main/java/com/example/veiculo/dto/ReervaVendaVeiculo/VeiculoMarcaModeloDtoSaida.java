package com.example.veiculo.dto.ReervaVendaVeiculo;

public record VeiculoMarcaModeloDtoSaida(String descricao, Long qtdeDisponivel, Long qtdeReservada, Long qtdeVendida, Long qtdeTotal) {
    public static VeiculoMarcaModeloDtoSaida ConverteDto(VeiculoMarcaModeloDtoSaida e) {
        return new VeiculoMarcaModeloDtoSaida(e.descricao(), e.qtdeDisponivel(), e.qtdeReservada(), e.qtdeVendida(), e.qtdeTotal());
    }
}