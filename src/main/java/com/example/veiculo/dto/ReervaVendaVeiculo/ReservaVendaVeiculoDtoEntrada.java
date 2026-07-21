package com.example.veiculo.dto.ReervaVendaVeiculo;

import com.example.veiculo.model.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record ReservaVendaVeiculoDtoEntrada(
        @NotNull(message = "Campo Id da veículo é obrigatório")
        Long veiculoId,
        @NotNull(message = "Campo Id da cliente é obrigatório")
        Long clienteId,
        @Digits(integer = 9, fraction = 2, message = "o tamanho do conteúdo do campo valor deve ser entre 1 e 9 posições")
        BigDecimal valor,
        OffsetDateTime dtOpera
) {
    public static ReservaVendaVeiculo ConverteDto(ReservaVendaVeiculoDtoEntrada dto, Long id) {
        ReservaVendaVeiculo tb = new ReservaVendaVeiculo();
        tb.setId(id);

        tb.setValor(dto.valor());
        tb.setDtOpera(dto.dtOpera());

        Veiculo veiculo = new Veiculo();
        veiculo.setId(dto.veiculoId());
        tb.setVeiculo(veiculo);

        Cliente cliente = new Cliente();
        cliente.setId(dto.clienteId());
        tb.setCliente(cliente);

        return tb;
    }
}