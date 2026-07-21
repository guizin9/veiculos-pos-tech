package com.example.veiculo.dto.Versao;

import com.example.veiculo.model.Versao;

import java.time.OffsetDateTime;

public record VersaoDtoSaida(Long id, String nome, Long modeloId, OffsetDateTime dtOpera) {
    public static VersaoDtoSaida ConverteDto(Versao e) {
        return new VersaoDtoSaida(e.getId(), e.getNome(), e.getModelo().getId(), e.getDtOpera());
    }
}