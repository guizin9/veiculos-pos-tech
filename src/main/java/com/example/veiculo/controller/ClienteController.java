package com.example.veiculo.controller;

import com.example.veiculo.controller.validator.ClienteDadosValidator;
import com.example.veiculo.dto.Cliente.ClienteDtoEntrada;
import com.example.veiculo.dto.Cliente.ClienteDtoSaida;
import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.geral.generic.GenerciController;
import com.example.veiculo.model.Cliente;
import com.example.veiculo.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("clientes")
public class ClienteController {

    private final ClienteService clienteService;
    private final ClienteDadosValidator clienteDadosValidator;

    @GetMapping("{id}")
    public ResponseEntity<ClienteDtoSaida> obterClientePorId(@PathVariable Long id) {
        return clienteService.obterClientePorId(id)
                .map(ClienteDtoSaida::ConverteDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<ClienteDtoSaida>> listaClientes() {
        return ResponseEntity.ok(
                clienteService.listaClientes()
                        .orElse(List.of()) // pega a lista ou cria vazia
                        .stream()
                        .map(ClienteDtoSaida::ConverteDto)
                        .toList());
    }

    @PostMapping
    public ResponseEntity<Object> incluir(@RequestBody @Valid ClienteDtoEntrada clienteDtoEntrada) {
        var tbEntrada = ClienteDtoEntrada.ConverteDto(clienteDtoEntrada, null);
        clienteDadosValidator.validaDados(new Cliente(), tbEntrada, Libs.TpOpe.INCLUSAO);
        clienteService.incluir(tbEntrada);
        return ResponseEntity.created(GenerciController.gerarHeaderLocation(tbEntrada.getId())).build(); //return new ResponseEntity("Autor Salvo com sucesso! " + autor, HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<Object> alterar(@PathVariable Long id, @RequestBody @Valid ClienteDtoEntrada clienteDtoEntrada) {
        return clienteService.obterClientePorId(id, true)
                .map(tbInterna -> {
                    Cliente tbEntrada = ClienteDtoEntrada.ConverteDto(clienteDtoEntrada, id); //  PessoaDtoEntrada.clonarDtoParaNovaEntidade(pessoaDtoEntrada);
                    clienteDadosValidator.validaDados(tbInterna, tbEntrada, Libs.TpOpe.ALTERACAO);
                    clienteService.alterar(tbInterna, tbEntrada);
                    return ResponseEntity.noContent().build();
                }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Object> excluir(@PathVariable Long id) {
        clienteDadosValidator.validaId(id, Libs.TpOpe.EXCLUSAO);
        if (!clienteService.excluir(id)) return ResponseEntity.notFound().build();
        return ResponseEntity.noContent().build();
    }
}
