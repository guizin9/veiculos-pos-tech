package com.example.veiculo.dto.Cor;

import com.example.veiculo.model.Cor;
import com.example.veiculo.model.Marca;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record CorDtoEntrada(
        @NotBlank(message = "Campo nome é obrigatório")
        @Size(min = 2, max = 70, message = "o tamanho do conteúdo do campo nome deve ser entre 2 e 30 posições")
        String nome,
        OffsetDateTime dtOpera
) {
    public static Cor ConverteDto(CorDtoEntrada dto, Long id) {
        Cor tb = new Cor();
        tb.setId(id);
        tb.setNome(dto.nome());
        tb.setDtOpera(dto.dtOpera());
        return tb;
    }
}