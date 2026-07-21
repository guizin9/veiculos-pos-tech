package com.example.veiculo.dto.Modelo;

import com.example.veiculo.model.Modelo;

import java.time.OffsetDateTime;

public record ModeloDtoSaida(Long id, String nome, Long marcaId, OffsetDateTime dtOpera) {
    public static ModeloDtoSaida ConverteDto(Modelo e) {
        return new ModeloDtoSaida(e.getId(), e.getNome(), e.getMarca().getId(), e.getDtOpera());
    }
}