package com.example.veiculo.controller;

import com.example.veiculo.controller.validator.ReservaVendaVeiculoDadosValidator;
import com.example.veiculo.dto.ReervaVendaVeiculo.*;
import com.example.veiculo.dto.Veiculo.VeiculoAVendaDtoSaida;
import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.geral.generic.GenerciController;
import com.example.veiculo.model.ReservaVendaVeiculo;
import com.example.veiculo.service.ReservaVendaVeiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("reserva-venda-veiculos")
public class ReservaVendaVeiculoController {

    private final ReservaVendaVeiculoService reservaVendaVeiculoService;
    private final ReservaVendaVeiculoDadosValidator reservaVendaVeiculoDadosValidator;

    @GetMapping("{id}")
    public ResponseEntity<ReservaVendaVeiculoDtoSaida> obterReservaVendaVeiculoPorId(@PathVariable Long id) {
        return reservaVendaVeiculoService.obterReservaVendaVeiculoPorId(id)
                .map(ReservaVendaVeiculoDtoSaida::ConverteDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ReservaVendaVeiculoDtoSaida>> listaVeiculosReservadosCanceladosVendidos() {
        return ResponseEntity.ok(
                reservaVendaVeiculoService.listaVeiculosReservadosCanceladosVendidos()
                        .orElse(List.of()) // pega a lista ou cria vazia
                        .stream()
                        .map(ReservaVendaVeiculoDtoSaida::ConverteDto)
                        .toList());
    }

    @GetMapping("reservados")
    public ResponseEntity<List<VeiculoReservadoDtoSaida>> listaVeiculo_Reservados() {
        return ResponseEntity.ok(
                reservaVendaVeiculoService.listaVeiculos_Reservados()
                        .orElse(List.of()) // pega a lista ou cria vazia
                        .stream()
                        .map(VeiculoReservadoDtoSaida::ConverteDto)
                        .toList());
    }

    @GetMapping("cancelados")
    public ResponseEntity<List<VeiculoCanceladoDtoSaida>> listaVeiculosCancelados() {
        return ResponseEntity.ok(
                reservaVendaVeiculoService.listaVeiculos_Cancelados()
                        .orElse(List.of()) // pega a lista ou cria vazia
                        .stream()
                        .map(VeiculoCanceladoDtoSaida::ConverteDto)
                        .toList());
    }

    @GetMapping("vendidos")
    public ResponseEntity<List<VeiculoVendidosDtoSaida>> listaVeiculosVendidos() {
        return ResponseEntity.ok(
                reservaVendaVeiculoService.listaVeiculos_Vendidos()
                        .orElse(List.of()) // pega a lista ou cria vazia
                        .stream()
                        .map(VeiculoVendidosDtoSaida::ConverteDto)
                        .toList());
    }

    @GetMapping("pendentes-de-retirada")
    public ResponseEntity<List<VeiculoVendidosDtoSaida>> listaVeiculosPendentesRetirada() {
        return ResponseEntity.ok(
                reservaVendaVeiculoService.listaVeiculos_Pendentes_Retirada()
                        .orElse(List.of()) // pega a lista ou cria vazia
                        .stream()
                        .map(VeiculoVendidosDtoSaida::ConverteDto)
                        .toList());
    }

    @GetMapping("retirados")
    public ResponseEntity<List<VeiculoVendidosDtoSaida>> listaVeiculosRetirado() {
        return ResponseEntity.ok(
                reservaVendaVeiculoService.listaVeiculos_Retirados()
                        .orElse(List.of()) // pega a lista ou cria vazia
                        .stream()
                        .map(VeiculoVendidosDtoSaida::ConverteDto)
                        .toList());
    }

    @GetMapping("marca-modelo-versao")
    public ResponseEntity<List<VeiculoMarcaModeloVersaoDtoSaida>> listaVeiculoMarcaModeloVersao() {
        return ResponseEntity.ok(
                reservaVendaVeiculoService.listaVeiculoMarcaModeloVersao()
                        .orElse(List.of()) // pega a lista ou cria vazia
                        .stream()
                        .map(VeiculoMarcaModeloVersaoDtoSaida::ConverteDto)
                        .toList());
    }

    @GetMapping("marca-modelo-versao-qtde-estoque")
    public ResponseEntity<List<VeiculoMarcaModeloVersaoQtdeEstoqueDtoSaida>> listaVeiculoMarcaModeloVersaoQtdeEstoque() {
        return ResponseEntity.ok(
                reservaVendaVeiculoService.listaVeiculoMarcaModeloVersaoQtdeEstoque()
                        .orElse(List.of()) // pega a lista ou cria vazia
                        .stream()
                        .map(VeiculoMarcaModeloVersaoQtdeEstoqueDtoSaida::ConverteDto)
                        .toList());
    }

    @GetMapping("marca-modelo")
    public ResponseEntity<List<VeiculoMarcaModeloDtoSaida>> listaVeiculoMarcaModelo() {
        return ResponseEntity.ok(
                reservaVendaVeiculoService.listaVeiculoMarcaModelo()
                        .orElse(List.of()) // pega a lista ou cria vazia
                        .stream()
                        .map(VeiculoMarcaModeloDtoSaida::ConverteDto)
                        .toList());
    }

    @GetMapping("marca")
    public ResponseEntity<List<VeiculoMarcaDtoSaida>> listaVeiculoMarca() {
        return ResponseEntity.ok(
                reservaVendaVeiculoService.listaVeiculoMarca()
                        .orElse(List.of()) // pega a lista ou cria vazia
                        .stream()
                        .map(VeiculoMarcaDtoSaida::ConverteDto)
                        .toList());
    }

    @PostMapping()
    @PreAuthorize("hasAnyRole('CLIENTE','VENDEDOR','ADMIN')")
    public ResponseEntity<Object> incluirReserva(@RequestBody @Valid ReservaVendaVeiculoDtoEntrada veiculoDtoEntrada) {
        var tbEntrada = ReservaVendaVeiculoDtoEntrada.ConverteDto(veiculoDtoEntrada, null);
        reservaVendaVeiculoDadosValidator.validaDados(new ReservaVendaVeiculo(), tbEntrada, Libs.TpOpe.INCLUSAO);
        reservaVendaVeiculoService.incluirReserva(tbEntrada);
        return ResponseEntity.created(GenerciController.gerarHeaderLocation(tbEntrada.getId())).body("Código da Reserva: " + tbEntrada.getId());
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAnyRole('VENDEDOR','ADMIN')")
    public ResponseEntity<Object> alterarReserva(@PathVariable Long id, @RequestBody @Valid ReservaVendaVeiculoDtoEntrada reservaVendaVeiculoDtoEntrada) {
        return reservaVendaVeiculoService.obterReservaVendaVeiculoPorId(id, true)
                .map(tbInterna -> {
                    ReservaVendaVeiculo tbEntrada = ReservaVendaVeiculoDtoEntrada.ConverteDto(reservaVendaVeiculoDtoEntrada, id); //  PessoaDtoEntrada.clonarDtoParaNovaEntidade(pessoaDtoEntrada);
                    reservaVendaVeiculoDadosValidator.validaDados(tbInterna, tbEntrada, Libs.TpOpe.ALTERACAO);
                    reservaVendaVeiculoService.alterarReserva(tbInterna, tbEntrada);
                    return ResponseEntity.noContent().build();
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("confirma-venda/{id}")
    @PreAuthorize("hasAnyRole('VENDEDOR','OPERADOR','ADMIN')")
    public ResponseEntity<Object> confirmaVenda(@PathVariable Long id) {
        return reservaVendaVeiculoService.obterReservaVendaVeiculoPorId(id, true)
                .map(tbInterna -> {
                    reservaVendaVeiculoService.confirmaVenda(tbInterna);
                    return ResponseEntity.noContent().build();
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("retira-veiculo/{id}")
    @PreAuthorize("hasAnyRole('VENDEDOR','OPERADOR','ADMIN')")
    public ResponseEntity<Object> retirarVeiculo(@PathVariable Long id) {
        return reservaVendaVeiculoService.obterReservaVendaVeiculoPorId(id, true)
                .map(tbInterna -> {
                    reservaVendaVeiculoService.retiraVeiculo(tbInterna);
                    return ResponseEntity.noContent().build();
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }
    @DeleteMapping("{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','VENDEDOR','ADMIN')")
    public ResponseEntity<Object> cancela(@PathVariable Long id) {
        return reservaVendaVeiculoService.obterReservaVendaVeiculoPorId(id, true)
                .map(tbInterna -> {
                    reservaVendaVeiculoService.cancela(tbInterna);
                    return ResponseEntity.noContent().build();
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
