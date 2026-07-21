package com.example.veiculo.dto.Versao;

import com.example.veiculo.model.Marca;
import com.example.veiculo.model.Modelo;
import com.example.veiculo.model.Versao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record VersaoDtoEntrada(
        @NotBlank(message = "Campo nome é obrigatório")
        @Size(min = 2, max = 70, message = "o tamanho do conteúdo do campo nome deve ser entre 2 e 70 posições")
        String nome,
        Long modeloId,
        OffsetDateTime dtOpera
) {
    public static Versao ConverteDto(VersaoDtoEntrada dto, Long id) {
        Versao tb = new Versao();
        tb.setId(id);
        tb.setNome(dto.nome());
        tb.setDtOpera(dto.dtOpera());

        Modelo modelo = new Modelo();
        modelo.setId(dto.modeloId());
        tb.setModelo(modelo);
        return tb;
    }
}