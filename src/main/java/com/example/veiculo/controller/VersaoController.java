package com.example.veiculo.controller;

import com.example.veiculo.controller.validator.VersaoDadosValidator;
import com.example.veiculo.dto.Versao.VersaoDtoEntrada;
import com.example.veiculo.dto.Versao.VersaoDtoSaida;
import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.geral.generic.GenerciController;
import com.example.veiculo.model.Versao;
import com.example.veiculo.service.VersaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("versoes")
public class VersaoController {

    private final VersaoService versaoService;
    private final VersaoDadosValidator versaoDadosValidator;

    @GetMapping("{id}")
    public ResponseEntity<VersaoDtoSaida> obterVersaoPorId(@PathVariable Long id) {
        return versaoService.obterVersaoPorId(id)
                .map(VersaoDtoSaida::ConverteDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<VersaoDtoSaida>> listaVersaos() {
        return ResponseEntity.ok(
                versaoService.listaVersaos()
                        .orElse(List.of()) // pega a lista ou cria vazia
                        .stream()
                        .map(VersaoDtoSaida::ConverteDto)
                        .toList());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<Object> incluir(@RequestBody @Valid VersaoDtoEntrada versaoDtoEntrada) {
        var tbEntrada = VersaoDtoEntrada.ConverteDto(versaoDtoEntrada, null);
        versaoDadosValidator.validaDados(new Versao(), tbEntrada, Libs.TpOpe.INCLUSAO);
        versaoService.incluir(tbEntrada);
        return ResponseEntity.created(GenerciController.gerarHeaderLocation(tbEntrada.getId())).build(); //return new ResponseEntity("Autor Salvo com sucesso! " + autor, HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<Object> alterar(@PathVariable Long id, @RequestBody @Valid VersaoDtoEntrada versaoDtoEntrada) {
        return versaoService.obterVersaoPorId(id, true)
                .map(tbInterna -> {
                    Versao tbEntrada = VersaoDtoEntrada.ConverteDto(versaoDtoEntrada, id); //  PessoaDtoEntrada.clonarDtoParaNovaEntidade(pessoaDtoEntrada);
                    versaoDadosValidator.validaDados(tbInterna, tbEntrada, Libs.TpOpe.ALTERACAO);
                    versaoService.alterar(tbInterna, tbEntrada);
                    return ResponseEntity.noContent().build();
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ResponseEntity<Object> excluir(@PathVariable Long id) {
        versaoDadosValidator.validaId(id, Libs.TpOpe.EXCLUSAO);
        if (!versaoService.excluir(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
