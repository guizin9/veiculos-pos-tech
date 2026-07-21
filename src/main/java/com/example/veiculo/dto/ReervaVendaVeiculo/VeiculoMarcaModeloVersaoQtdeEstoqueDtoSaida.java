package com.example.veiculo.dto.ReervaVendaVeiculo;

public record VeiculoMarcaModeloVersaoQtdeEstoqueDtoSaida(String descricao, Long qtdeEstoque) {
    public static VeiculoMarcaModeloVersaoQtdeEstoqueDtoSaida ConverteDto(VeiculoMarcaModeloVersaoQtdeEstoqueDtoSaida e) {
        return new VeiculoMarcaModeloVersaoQtdeEstoqueDtoSaida(e.descricao, e.qtdeEstoque());
    }
}