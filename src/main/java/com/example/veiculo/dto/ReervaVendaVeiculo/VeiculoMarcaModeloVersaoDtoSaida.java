package com.example.veiculo.dto.ReervaVendaVeiculo;

public record VeiculoMarcaModeloVersaoDtoSaida(String descricao, Long qtdeDisponivel, Long qtdeReservada, Long qtdeVendida, Long qtdeTotal) {
    public static VeiculoMarcaModeloVersaoDtoSaida ConverteDto(VeiculoMarcaModeloVersaoDtoSaida e) {
        return new VeiculoMarcaModeloVersaoDtoSaida(e.descricao, e.qtdeDisponivel(), e.qtdeReservada(), e.qtdeVendida(), e.qtdeTotal());
    }
}