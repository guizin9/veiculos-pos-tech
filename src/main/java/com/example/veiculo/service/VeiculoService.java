package com.example.veiculo.service;

import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.model.Veiculo;
import com.example.veiculo.repository.VeiculoRepository;
import com.example.veiculo.service.validator.VeiculoNegocioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final VeiculoNegocioValidator veiculoNegocioValidator;

    public Optional<Veiculo> obterVeiculoPorId(Long id) { return obterVeiculoPorId(id, false); }
    public Optional<Veiculo> obterVeiculoPorId(Long id, boolean validaID) {
        com.example.veiculo.geral.config.Libs.isIdNull(id);
        var ret = veiculoRepository.findById(id);
        if (!validaID) if (ret.isEmpty()) throw new RegistroNaoEncontradoException("Veiculo com ID " + id + " não encontrada");
        return ret;
    }

    public Optional<List<Veiculo>> listaVeiculos() {
        return Optional.of(veiculoRepository.findAll(Sort.by("id")));
    }

    public Optional<List<Veiculo>> listaVeiculos_A_Venda() {
        return Optional.of(veiculoRepository.findByStatusOrderByValorAsc("A", Sort.by("id")));
    }

    public Veiculo obterPorChassi(String chassi) {
        return veiculoRepository.findByChassi(chassi).get();
    }

    @Transactional
    public Veiculo incluir(Veiculo tbEntrada) {
        veiculoNegocioValidator.validarNegocioInclusao(tbEntrada);
        tbEntrada.ativaVeiculo();
        tbEntrada.setDtOpera(OffsetDateTime.now());
        return veiculoRepository.save(tbEntrada);
    }

    @Transactional
    public void alterar(Veiculo tbInterna, Veiculo tbEntrada) {
        veiculoNegocioValidator.validarNegocioAlteracao(tbEntrada);
        tbEntrada.setDtOpera(OffsetDateTime.now());
        copiarTabela(tbEntrada, tbInterna);
        veiculoRepository.save(tbInterna);
    }

    @Transactional
    public boolean excluir(Long id) {
        veiculoNegocioValidator.validarNegocioExclusao(id);
        veiculoRepository.deleteById(id);
        return true;
    }

    private void copiarTabela(Veiculo tbOrigem, Veiculo tbDestino) {
        tbDestino.setAnoFabricacao(tbOrigem.getAnoFabricacao());
        tbDestino.setAnoModelo(tbOrigem.getAnoModelo());
        tbDestino.setValor(tbOrigem.getValor());
        tbDestino.setChassi(tbOrigem.getChassi());
        tbDestino.setCor(tbOrigem.getCor());
        tbDestino.setVersao(tbOrigem.getVersao());
        tbDestino.setDtOpera(tbOrigem.getDtOpera());

    }
}
