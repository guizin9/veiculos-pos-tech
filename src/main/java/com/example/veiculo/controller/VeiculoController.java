package com.example.veiculo.controller;

import com.example.veiculo.controller.validator.VeiculoDadosValidator;
import com.example.veiculo.dto.ReervaVendaVeiculo.ReservaVeiculoDtoSaida;
import com.example.veiculo.dto.Veiculo.VeiculoAVendaDtoSaida;
import com.example.veiculo.dto.Veiculo.VeiculoDtoEntrada;
import com.example.veiculo.dto.Veiculo.VeiculoDtoSaida;
import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.geral.generic.GenerciController;
import com.example.veiculo.model.Veiculo;
import com.example.veiculo.service.VeiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("veiculos")
public class VeiculoController {

    private final VeiculoService veiculoService;
    private final VeiculoDadosValidator veiculoDadosValidator;

    @GetMapping("{id}")
    public ResponseEntity<VeiculoDtoSaida> obterVeiculoPorId(@PathVariable Long id) {
        return veiculoService.obterVeiculoPorId(id)
                .map(VeiculoDtoSaida::ConverteDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<VeiculoDtoSaida>> listaVeiculos() {
        return ResponseEntity.ok(
                veiculoService.listaVeiculos()
                        .orElse(List.of()) // pega a lista ou cria vazia
                        .stream()
                        .map(VeiculoDtoSaida::ConverteDto)
                        .toList());
    }

    @GetMapping("a-venda")
    public ResponseEntity<List<VeiculoAVendaDtoSaida>> listaVeiculos_A_Venda() {
        return ResponseEntity.ok(
                veiculoService.listaVeiculos_A_Venda()
                        .orElse(List.of()) // pega a lista ou cria vazia
                        .stream()
                        .map(VeiculoAVendaDtoSaida::ConverteDto)
                        .toList());
    }

    @PostMapping()
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<Object> incluir(@RequestBody @Valid VeiculoDtoEntrada veiculoDtoEntrada) {
        var tbEntrada = VeiculoDtoEntrada.ConverteDto(veiculoDtoEntrada, null);
        veiculoDadosValidator.validaDados(new Veiculo(), tbEntrada, Libs.TpOpe.INCLUSAO);
        veiculoService.incluir(tbEntrada);
        return ResponseEntity.created(GenerciController.gerarHeaderLocation(tbEntrada.getId())).build(); //return new ResponseEntity("Autor Salvo com sucesso! " + autor, HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<Object> alterar(@PathVariable Long id, @RequestBody @Valid VeiculoDtoEntrada veiculoDtoEntrada) {
        return veiculoService.obterVeiculoPorId(id, true)
                .map(tbInterna -> {
                    Veiculo tbEntrada = VeiculoDtoEntrada.ConverteDto(veiculoDtoEntrada, id); //  PessoaDtoEntrada.clonarDtoParaNovaEntidade(pessoaDtoEntrada);
                    veiculoDadosValidator.validaDados(tbInterna, tbEntrada, Libs.TpOpe.ALTERACAO);
                    veiculoService.alterar(tbInterna, tbEntrada);
                    return ResponseEntity.noContent().build();
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<Object> excluir(@PathVariable Long id) {
        veiculoDadosValidator.validaId(id, Libs.TpOpe.EXCLUSAO);
        if (!veiculoService.excluir(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
