package com.example.veiculo.service;

import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.model.Modelo;
import com.example.veiculo.repository.ModeloRepository;
import com.example.veiculo.service.validator.ModeloNegocioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ModeloService {

    private final ModeloRepository modeloRepository;
    private final ModeloNegocioValidator modeloNegocioValidator;

    public Optional<Modelo> obterModeloPorId(Long id) { return obterModeloPorId(id, false); }
    public Optional<Modelo> obterModeloPorId(Long id, boolean validaID) {
        com.example.veiculo.geral.config.Libs.isIdNull(id);
        var ret = modeloRepository.findById(id);
        if (!validaID) if (ret.isEmpty()) throw new RegistroNaoEncontradoException("Modelo com ID " + id + " não encontrada");
        return ret;
    }

    public Optional<List<Modelo>> listaModelos() {
        return Optional.of(modeloRepository.findAll(Sort.by("nome")));
    }

    public Modelo obterPorNome(String nome) {
        return modeloRepository.findByNome(nome).get();
    }

    @Transactional
    public Modelo incluir(Modelo tbEntrada) {
        modeloNegocioValidator.validarNegocioInclusao(tbEntrada);
        tbEntrada.setDtOpera(OffsetDateTime.now());
        return modeloRepository.save(tbEntrada);
    }

    @Transactional
    public void alterar(Modelo tbInterna, Modelo tbEntrada) {
        modeloNegocioValidator.validarNegocioAlteracao(tbEntrada);
        tbEntrada.setDtOpera(OffsetDateTime.now());
        copiarTabela(tbEntrada, tbInterna);
        modeloRepository.save(tbInterna);
    }

    @Transactional
    public boolean excluir(Long id) {
        modeloNegocioValidator.validarNegocioExclusao(id);
        modeloRepository.deleteById(id);
        return true;
    }

    private void copiarTabela(Modelo tbOrigem, Modelo tbDestino) {
        tbDestino.setNome(tbOrigem.getNome());
        tbDestino.setDtOpera(tbOrigem.getDtOpera());
    }
}
