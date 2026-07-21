package com.example.veiculo.service;

import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.model.Versao;
import com.example.veiculo.repository.VersaoRepository;
import com.example.veiculo.service.validator.VersaoNegocioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VersaoService {

    private final VersaoRepository versaoRepository;
    private final VersaoNegocioValidator versaoNegocioValidator;

    public Optional<Versao> obterVersaoPorId(Long id) { return obterVersaoPorId(id, false); }
    public Optional<Versao> obterVersaoPorId(Long id, boolean validaID) {
        com.example.veiculo.geral.config.Libs.isIdNull(id);
        var ret = versaoRepository.findById(id);
        if (!validaID) if (ret.isEmpty()) throw new RegistroNaoEncontradoException("Versao com ID " + id + " não encontrada");
        return ret;
    }

    public Optional<List<Versao>> listaVersaos() {
        return Optional.of(versaoRepository.findAll(Sort.by("nome")));
    }

    public Versao obterPorNome(String nome) {
        return versaoRepository.findByNome(nome).get();
    }

    @Transactional
    public Versao incluir(Versao tbEntrada) {
        versaoNegocioValidator.validarNegocioInclusao(tbEntrada);
        tbEntrada.setDtOpera(OffsetDateTime.now());
        return versaoRepository.save(tbEntrada);
    }

    @Transactional
    public void alterar(Versao tbInterna, Versao tbEntrada) {
        versaoNegocioValidator.validarNegocioAlteracao(tbEntrada);
        tbEntrada.setDtOpera(OffsetDateTime.now());
        copiarTabela(tbEntrada, tbInterna);
        versaoRepository.save(tbInterna);
    }

    @Transactional
    public boolean excluir(Long id) {
        versaoNegocioValidator.validarNegocioExclusao(id);
        versaoRepository.deleteById(id);
        return true;
    }

    private void copiarTabela(Versao tbOrigem, Versao tbDestino) {
        tbDestino.setNome(tbOrigem.getNome());
        tbDestino.setDtOpera(tbOrigem.getDtOpera());
    }
}
