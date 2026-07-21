package com.example.veiculo.dto.Modelo;

import com.example.veiculo.model.Marca;
import com.example.veiculo.model.Modelo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record ModeloDtoEntrada(
        @NotBlank(message = "Campo nome é obrigatório")
        @Size(min = 2, max = 70, message = "o tamanho do conteúdo do campo nome deve ser entre 2 e 70 posições")
        String nome,
        Long marcaId,
        OffsetDateTime dtOpera
) {
    public static Modelo ConverteDto(ModeloDtoEntrada dto, Long id) {
        Modelo tb = new Modelo();
        tb.setId(id);
        tb.setNome(dto.nome());
        tb.setDtOpera(dto.dtOpera());

        Marca marca = new Marca();
        marca.setId(dto.marcaId());
        tb.setMarca(marca);
        return tb;
    }
}