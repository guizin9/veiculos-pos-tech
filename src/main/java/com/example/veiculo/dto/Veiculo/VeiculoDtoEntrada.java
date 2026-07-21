package com.example.veiculo.dto.Veiculo;

import com.example.veiculo.model.Cor;
import com.example.veiculo.model.Veiculo;
import com.example.veiculo.model.Versao;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record VeiculoDtoEntrada(
//        @NotBlank(message = "Campo status é obrigatório")
//        String status,
        @NotNull(message = "Campo Id da cor é obrigatório")
        Long corId,
        @NotNull(message = "Campo Id da versão é obrigatório")
        Long versaoId,
        @Digits(integer = 4, fraction = 0, message = "o tamanho do conteúdo do campo ano da fabricação deve ser de 4 posições")
        Short anoFabricacao,
        @Digits(integer = 4, fraction = 0, message = "o tamanho do conteúdo do campo ano do modelo deve ser de 4 posições")
        Short anoModelo,
        @Size(min = 10, max = 17, message = "o tamanho do conteúdo do campo ano do modelo deve ser entre 10 e 17 posições")
        String chassi,
        @Digits(integer = 9, fraction = 2, message = "o tamanho do conteúdo do campo valor deve ser entre 1 e 9 posições")
        BigDecimal valor,
        OffsetDateTime dtOpera
) {
    public static Veiculo ConverteDto(VeiculoDtoEntrada dto, Long id) {
        Veiculo tb = new Veiculo();
        tb.setId(id);

        tb.setAnoFabricacao(dto.anoFabricacao());
        tb.setAnoModelo(dto.anoModelo());
        tb.setChassi(dto.chassi());
        tb.setValor(dto.valor());

        Cor cor = new Cor();
        cor.setId(dto.corId());
        tb.setCor(cor);

        Versao versao = new Versao();
        versao.setId(dto.versaoId());
        tb.setVersao(versao);

        return tb;
    }
}