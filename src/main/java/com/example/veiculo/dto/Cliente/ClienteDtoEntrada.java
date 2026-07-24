package com.example.veiculo.dto.Cliente;

import com.example.veiculo.model.Cliente;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;

public record ClienteDtoEntrada(
        @NotBlank(message = "Campo nome é obrigatório")
        @Size(min = 2, max = 70, message = "o tamanho do conteúdo do campo nome deve ser entre 2 e 70 posições")
        String nome,
        @Size(min = 1, max = 15, message = "o tamanho do conteúdo do campo CPF deve ser entre 1 e 15 posições")
        String cpf,


        @Size(min = 1, max = 50, message = "o tamanho do conteúdo do campo logradouro deve ser entre 1 e 50 posições")
        String logradouro,
        @Size(min = 1, max = 10, message = "o tamanho do conteúdo do campo número do endereço deve ser entre 1 e 10 posições")
        String numero,

        @Size(min = 0, max = 20, message = "o tamanho do conteúdo do campo complemento do endereço não pode ser maior que 20 posições")
        String complemento,

        @Size(min = 1, max = 30, message = "o tamanho do conteúdo do campo bairro deve ser entre 1 e 30 posições")
        String bairro,

        @Size(min = 1, max = 40, message = "o tamanho do conteúdo do campo cidade deve ser entre 1 e 40 posições")
        String cidade,

        @Size(min = 2, max = 3, message = "o tamanho do conteúdo do campo estado deve ser entre 2 e 3 posições")
        String estado,

        @Size(min = 8, max = 8, message = "o tamanho do conteúdo do campo cep deve ser conter 8 posições")
        String cep,

        @Size(min = 3, max = 15, message = "o tamanho do conteúdo do campo celular deve ser entre 3 e 15 posições")
        @JsonAlias("celular")
        String celula,

        @Size(min = 3, max = 15, message = "o tamanho do conteúdo do campo fone fixo deve ser entre 3 e 15 posições")
        String foneFixo,

        @Size(min = 1, max = 50, message = "o tamanho do conteúdo do campo e-mail deve ser entre 1 e 50 posições")
        String email,
        OffsetDateTime dtOpera
) {
    public static Cliente ConverteDto(ClienteDtoEntrada dto, Long id) {
        Cliente tb = new Cliente();
        tb.setId(id);
        tb.setNome(dto.nome());
        tb.setCpf(dto.cpf());

        tb.setLogradouro(dto.logradouro());
        tb.setNumero(dto.numero());
        tb.setComplemento(dto.complemento());
        tb.setBairro(dto.bairro());
        tb.setCidade(dto.cidade());
        tb.setEstado(dto.estado());
        tb.setCep(dto.cep());
        tb.setCelular(dto.celula());
        tb.setFoneFixo(dto.foneFixo());
        tb.setEmail(dto.email());

        tb.setDtOpera(dto.dtOpera());
        return tb;
    }
}