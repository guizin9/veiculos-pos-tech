package com.example.veiculo.geral.config.exception.dto;

import org.springframework.http.HttpStatus;

import java.util.List;

public record ErroRespostaDto(int status, String mensagem, List<ErroCampoDto> erros) {

    public static ErroRespostaDto respostaPadrao(String mensagem) {
        return new ErroRespostaDto(HttpStatus.BAD_REQUEST.value(), mensagem, List.of());
    }

    public static ErroRespostaDto conflito(String mensagem) {
        return new ErroRespostaDto(HttpStatus.CONFLICT.value(),  mensagem, List.of());
    }

}
