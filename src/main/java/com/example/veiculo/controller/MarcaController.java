package com.example.veiculo.controller;

import com.example.veiculo.controller.validator.MarcaDadosValidator;
import com.example.veiculo.dto.Marca.MarcaDtoEntrada;
import com.example.veiculo.dto.Marca.MarcaDtoSaida;
import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.geral.generic.GenerciController;
import com.example.veiculo.model.Marca;
import com.example.veiculo.service.MarcaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("marcas")
public class MarcaController {

    private final MarcaService marcaService;
    private final MarcaDadosValidator marcaDadosValidator;

    @GetMapping("{id}")
    public ResponseEntity<MarcaDtoSaida> obterMarcaPorId(@PathVariable Long id) {
        return marcaService.obterMarcaPorId(id)
                .map(MarcaDtoSaida::ConverteDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<MarcaDtoSaida>> listaMarcas() {
        return ResponseEntity.ok(
                marcaService.listaMarcas()
                        .orElse(List.of()) // pega a lista ou cria vazia
                        .stream()
                        .map(MarcaDtoSaida::ConverteDto)
                        .toList());
    }

    @PostMapping
    public ResponseEntity<Object> incluir(@RequestBody @Valid MarcaDtoEntrada marcaDtoEntrada) {
        var tbEntrada = MarcaDtoEntrada.ConverteDto(marcaDtoEntrada, null);
        marcaDadosValidator.validaDados(new Marca(), tbEntrada, Libs.TpOpe.INCLUSAO);
        marcaService.incluir(tbEntrada);
        return ResponseEntity.created(GenerciController.gerarHeaderLocation(tbEntrada.getId())).build(); //return new ResponseEntity("Autor Salvo com sucesso! " + autor, HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<Object> alterar(@PathVariable Long id, @RequestBody @Valid MarcaDtoEntrada marcaDtoEntrada) {
        return marcaService.obterMarcaPorId(id, true)
                .map(tbInterna -> {
                    Marca tbEntrada = MarcaDtoEntrada.ConverteDto(marcaDtoEntrada, id); //  PessoaDtoEntrada.clonarDtoParaNovaEntidade(pessoaDtoEntrada);
                    marcaDadosValidator.validaDados(tbInterna, tbEntrada, Libs.TpOpe.ALTERACAO);
                    marcaService.alterar(tbInterna, tbEntrada);
                    return ResponseEntity.noContent().build();
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Object> excluir(@PathVariable Long id) {
        marcaDadosValidator.validaId(id, Libs.TpOpe.EXCLUSAO);
        if (!marcaService.excluir(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
