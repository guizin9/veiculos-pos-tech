package com.example.veiculo.service;

import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.model.Cor;
import com.example.veiculo.repository.CorRepository;
import com.example.veiculo.service.validator.CorNegocioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class CorService {

    private final CorRepository corRepository;
    private final CorNegocioValidator corNegocioValidator;

    public Optional<Cor> obterCorPorId(Long id) { return obterCorPorId(id, false); }
    public Optional<Cor> obterCorPorId(Long id, boolean validaID) {
        com.example.veiculo.geral.config.Libs.isIdNull(id);
        var ret = corRepository.findById(id);
        if (!validaID) if (ret.isEmpty()) throw new RegistroNaoEncontradoException("Cor com ID " + id + " não encontrada");
        return ret;
    }

    public Optional<List<Cor>> listaCors() {
        return Optional.of(corRepository.findAll(Sort.by("nome")));
    }

    public Cor obterPorNome(String nome) {
        return corRepository.findByNome(nome).get();
    }

    @Transactional
    public Cor incluir(Cor tbEntrada) {
        corNegocioValidator.validarNegocioInclusao(tbEntrada);
        tbEntrada.setDtOpera(OffsetDateTime.now());
        return corRepository.save(tbEntrada);
    }

    @Transactional
    public void alterar(Cor tbInterna, Cor tbEntrada) {
        corNegocioValidator.validarNegocioAlteracao(tbEntrada);
        tbEntrada.setDtOpera(OffsetDateTime.now());
        copiarTabela(tbEntrada, tbInterna);
        corRepository.save(tbInterna);
    }

    @Transactional
    public boolean excluir(Long id) {
        corNegocioValidator.validarNegocioExclusao(id);
        corRepository.deleteById(id);
        return true;
    }

    private void copiarTabela(Cor tbOrigem, Cor tbDestino) {
        tbDestino.setNome(tbOrigem.getNome());
        tbDestino.setDtOpera(tbOrigem.getDtOpera());
    }
}
