package com.example.veiculo.dto.Cor;

import com.example.veiculo.model.Cor;

import java.time.OffsetDateTime;

public record CorDtoSaida(Long id, String nome, OffsetDateTime dtOpera) {
    public static CorDtoSaida ConverteDto(Cor e) {
        return new CorDtoSaida(e.getId(), e.getNome(), e.getDtOpera());
    }
}