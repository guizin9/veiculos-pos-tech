package com.example.veiculo.controller;

import com.example.veiculo.controller.validator.ModeloDadosValidator;
import com.example.veiculo.dto.Modelo.ModeloDtoEntrada;
import com.example.veiculo.dto.Modelo.ModeloDtoSaida;
import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.geral.generic.GenerciController;
import com.example.veiculo.model.Modelo;
import com.example.veiculo.service.ModeloService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("modelos")
public class ModeloController {

    private final ModeloService modeloService;
    private final ModeloDadosValidator modeloDadosValidator;

    @GetMapping("{id}")
    public ResponseEntity<ModeloDtoSaida> obterModeloPorId(@PathVariable Long id) {
        return modeloService.obterModeloPorId(id)
                .map(ModeloDtoSaida::ConverteDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ModeloDtoSaida>> listaModelos() {
        return ResponseEntity.ok(
                modeloService.listaModelos()
                        .orElse(List.of()) // pega a lista ou cria vazia
                        .stream()
                        .map(ModeloDtoSaida::ConverteDto)
                        .toList());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<Object> incluir(@RequestBody @Valid ModeloDtoEntrada modeloDtoEntrada) {
        var tbEntrada = ModeloDtoEntrada.ConverteDto(modeloDtoEntrada, null);
        modeloDadosValidator.validaDados(new Modelo(), tbEntrada, Libs.TpOpe.INCLUSAO);
        modeloService.incluir(tbEntrada);
        return ResponseEntity.created(GenerciController.gerarHeaderLocation(tbEntrada.getId())).build(); //return new ResponseEntity("Autor Salvo com sucesso! " + autor, HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<Object> alterar(@PathVariable Long id, @RequestBody @Valid ModeloDtoEntrada modeloDtoEntrada) {
        return modeloService.obterModeloPorId(id, true)
                .map(tbInterna -> {
                    Modelo tbEntrada = ModeloDtoEntrada.ConverteDto(modeloDtoEntrada, id); //  PessoaDtoEntrada.clonarDtoParaNovaEntidade(pessoaDtoEntrada);
                    modeloDadosValidator.validaDados(tbInterna, tbEntrada, Libs.TpOpe.ALTERACAO);
                    modeloService.alterar(tbInterna, tbEntrada);
                    return ResponseEntity.noContent().build();
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<Object> excluir(@PathVariable Long id) {
        modeloDadosValidator.validaId(id, Libs.TpOpe.EXCLUSAO);
        if (!modeloService.excluir(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
