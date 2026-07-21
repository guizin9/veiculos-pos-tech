package com.example.veiculo.dto.Marca;

import com.example.veiculo.model.Marca;

import java.time.OffsetDateTime;

public record MarcaDtoSaida(Long id, String nome, OffsetDateTime dtOpera) {
    public static MarcaDtoSaida ConverteDto(Marca e) {
        return new MarcaDtoSaida(e.getId(), e.getNome(), e.getDtOpera());
    }
}