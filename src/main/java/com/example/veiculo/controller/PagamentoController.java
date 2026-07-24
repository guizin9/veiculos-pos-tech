package com.example.veiculo.controller;

import com.example.veiculo.dto.Pagamento.PagamentoDtoSaida;
import com.example.veiculo.service.PagamentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("pagamentos")
public class PagamentoController {

    private final PagamentoService pagamentoService;

    @GetMapping
    public ResponseEntity<List<PagamentoDtoSaida>> listar() {
        return ResponseEntity.ok(
                pagamentoService.listar().stream()
                        .map(PagamentoDtoSaida::ConverteDto)
                        .toList());
    }

    @GetMapping("{id}")
    public ResponseEntity<PagamentoDtoSaida> obterPorId(@PathVariable Long id) {
        return pagamentoService.obterPorId(id)
                .map(PagamentoDtoSaida::ConverteDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("codigo/{codigo}")
    public ResponseEntity<PagamentoDtoSaida> obterPorCodigo(@PathVariable String codigo) {
        return ResponseEntity.ok(PagamentoDtoSaida.ConverteDto(pagamentoService.obterPorCodigo(codigo)));
    }

    @GetMapping("reserva/{reservaId}")
    public ResponseEntity<List<PagamentoDtoSaida>> listarPorReserva(@PathVariable Long reservaId) {
        return ResponseEntity.ok(
                pagamentoService.listarPorReserva(reservaId).stream()
                        .map(PagamentoDtoSaida::ConverteDto)
                        .toList());
    }

    @PostMapping("gerar/{reservaId}")
    public ResponseEntity<PagamentoDtoSaida> gerar(@PathVariable Long reservaId) {
        var pagamento = pagamentoService.gerarParaReservaId(reservaId);
        return ResponseEntity.ok(PagamentoDtoSaida.ConverteDto(pagamento));
    }

    @PostMapping("pagar/{codigo}")
    public ResponseEntity<PagamentoDtoSaida> pagar(@PathVariable String codigo) {
        var pagamento = pagamentoService.confirmarPagamento(codigo);
        return ResponseEntity.ok(PagamentoDtoSaida.ConverteDto(pagamento));
    }
}
