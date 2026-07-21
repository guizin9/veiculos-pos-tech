package com.example.veiculo.service;

import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.model.Marca;
import com.example.veiculo.repository.MarcaRepository;
import com.example.veiculo.service.validator.MarcaNegocioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarcaService {

    private final MarcaRepository marcaRepository;
    private final MarcaNegocioValidator marcaNegocioValidator;

    public Optional<Marca> obterMarcaPorId(Long id) { return obterMarcaPorId(id, false); }
    public Optional<Marca> obterMarcaPorId(Long id, boolean validaID) {
        com.example.veiculo.geral.config.Libs.isIdNull(id);
        var ret = marcaRepository.findById(id);
        if (!validaID) if (ret.isEmpty()) throw new RegistroNaoEncontradoException("Marca com ID " + id + " não encontrada");
        return ret;
    }

    public Optional<List<Marca>> listaMarcas() {
        return Optional.of(marcaRepository.findAll(Sort.by("nome")));
    }

    public Marca obterPorNome(String nome) {
        return marcaRepository.findByNome(nome).get();
    }

    @Transactional
    public Marca incluir(Marca tbEntrada) {
        marcaNegocioValidator.validarNegocioInclusao(tbEntrada);
        tbEntrada.setDtOpera(OffsetDateTime.now());
        return marcaRepository.save(tbEntrada);
    }

    @Transactional
    public void alterar(Marca tbInterna, Marca tbEntrada) {
        marcaNegocioValidator.validarNegocioAlteracao(tbEntrada);
        tbEntrada.setDtOpera(OffsetDateTime.now());
        copiarTabela(tbEntrada, tbInterna);
        marcaRepository.save(tbInterna);
    }

    @Transactional
    public boolean excluir(Long id) {
        marcaNegocioValidator.validarNegocioExclusao(id);
        marcaRepository.deleteById(id);
        return true;
    }

    private void copiarTabela(Marca tbOrigem, Marca tbDestino) {
        tbDestino.setNome(tbOrigem.getNome());
        tbDestino.setDtOpera(tbOrigem.getDtOpera());
    }
}
