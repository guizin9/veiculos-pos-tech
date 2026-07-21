package com.example.veiculo.dto.Cliente;

import com.example.veiculo.model.Cliente;

import java.time.OffsetDateTime;

public record ClienteDtoSaida(Long id, String nome, String cpf, String logradouro, String numero, String complemento, String bairro, String cidade, String estado, String cep, String celular, String foneFixo, String email, OffsetDateTime dtOpera) {
    public static ClienteDtoSaida ConverteDto(Cliente e) {
        return new ClienteDtoSaida(e.getId(), e.getNome(), e.getCpf(), e.getLogradouro(), e.getNumero(), e.getComplemento(), e.getBairro(), e.getCidade(), e.getEstado(), e.getCep(), e.getCelular(), e.getFoneFixo(), e.getEmail(), e.getDtOpera());
    }
}