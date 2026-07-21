package com.example.veiculo.controller;

import com.example.veiculo.controller.validator.CorDadosValidator;
import com.example.veiculo.dto.Cor.CorDtoEntrada;
import com.example.veiculo.dto.Cor.CorDtoSaida;
import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.geral.generic.GenerciController;
import com.example.veiculo.model.Cor;
import com.example.veiculo.service.CorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("cores")
public class CorController {

    private final CorService corService;
    private final CorDadosValidator corDadosValidator;

    @GetMapping("{id}")
    public ResponseEntity<CorDtoSaida> obterCorPorId(@PathVariable Long id) {
        return corService.obterCorPorId(id)
                .map(CorDtoSaida::ConverteDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CorDtoSaida>> listaCors() {
        return ResponseEntity.ok(
                corService.listaCors()
                        .orElse(List.of()) // pega a lista ou cria vazia
                        .stream()
                        .map(CorDtoSaida::ConverteDto)
                        .toList());
    }

    @PostMapping
    public ResponseEntity<Object> incluir(@RequestBody @Valid CorDtoEntrada corDtoEntrada) {
        var tbEntrada = CorDtoEntrada.ConverteDto(corDtoEntrada, null);
        corDadosValidator.validaDados(new Cor(), tbEntrada, Libs.TpOpe.INCLUSAO);
        corService.incluir(tbEntrada);
        return ResponseEntity.created(GenerciController.gerarHeaderLocation(tbEntrada.getId())).build(); //return new ResponseEntity("Autor Salvo com sucesso! " + autor, HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<Object> alterar(@PathVariable Long id, @RequestBody @Valid CorDtoEntrada corDtoEntrada) {
        return corService.obterCorPorId(id, true)
                .map(tbInterna -> {
                    Cor tbEntrada = CorDtoEntrada.ConverteDto(corDtoEntrada, id); //  PessoaDtoEntrada.clonarDtoParaNovaEntidade(pessoaDtoEntrada);
                    corDadosValidator.validaDados(tbInterna, tbEntrada, Libs.TpOpe.ALTERACAO);
                    corService.alterar(tbInterna, tbEntrada);
                    return ResponseEntity.noContent().build();
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Object> excluir(@PathVariable Long id) {
        corDadosValidator.validaId(id, Libs.TpOpe.EXCLUSAO);
        if (!corService.excluir(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
