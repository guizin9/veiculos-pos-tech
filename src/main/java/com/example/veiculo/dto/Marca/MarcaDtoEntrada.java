package com.example.veiculo.dto.Marca;

import com.example.veiculo.model.Marca;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record MarcaDtoEntrada(
        @NotBlank(message = "Campo nome é obrigatório")
        @Size(min = 2, max = 70, message = "o tamanho do conteúdo do campo nome deve ser entre 2 e 70 posições")
        String nome,
        OffsetDateTime dtOpera
) {
    public static Marca ConverteDto(MarcaDtoEntrada dto, Long id) {
        Marca tb = new Marca();
        tb.setId(id);
        tb.setNome(dto.nome());
        tb.setDtOpera(dto.dtOpera());
        return tb;
    }
}