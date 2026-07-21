package com.example.veiculo.service;

import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.model.Cliente;
import com.example.veiculo.repository.ClienteRepository;
import com.example.veiculo.service.validator.ClienteNegocioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ClienteNegocioValidator clienteNegocioValidator;

    public Optional<Cliente> obterClientePorId(Long id) { return obterClientePorId(id, false); }
    public Optional<Cliente> obterClientePorId(Long id, boolean validaID) {
        com.example.veiculo.geral.config.Libs.isIdNull(id);
        var ret = clienteRepository.findById(id);
        if (!validaID) if (ret.isEmpty()) throw new RegistroNaoEncontradoException("Cliente com ID " + id + " não encontrada");
        return ret;
    }

    public Optional<List<Cliente>> listaClientes() {
        return Optional.of(clienteRepository.findAll(Sort.by("nome")));
    }

    public Cliente obterPorNome(String nome) {
        return clienteRepository.findByNome(nome).get();
    }

    @Transactional
    public Cliente incluir(Cliente tbEntrada) {
        clienteNegocioValidator.validarNegocioInclusao(tbEntrada);
        tbEntrada.setDtOpera(OffsetDateTime.now());
        return clienteRepository.save(tbEntrada);
    }

    @Transactional
    public void alterar(Cliente tbInterna, Cliente tbEntrada) {
        clienteNegocioValidator.validarNegocioAlteracao(tbEntrada);
        tbEntrada.setDtOpera(OffsetDateTime.now());
        copiarTabela(tbEntrada, tbInterna);
        clienteRepository.save(tbInterna);
    }

    @Transactional
    public boolean excluir(Long id) {
        clienteNegocioValidator.validarNegocioExclusao(id);
        clienteRepository.deleteById(id);
        return true;
    }

    private void copiarTabela(Cliente tbOrigem, Cliente tbDestino) {
        tbDestino.setNome(tbOrigem.getNome());
        tbDestino.setCpf(tbOrigem.getCpf());
        tbDestino.setLogradouro(tbOrigem.getLogradouro());
        tbDestino.setNumero(tbOrigem.getNumero());
        tbDestino.setComplemento(tbOrigem.getComplemento());
        tbDestino.setBairro(tbOrigem.getBairro());
        tbDestino.setCidade(tbOrigem.getCidade());
        tbDestino.setEstado(tbOrigem.getEstado());
        tbDestino.setCep(tbOrigem.getCep());
        tbDestino.setCelular(tbOrigem.getCelular());
        tbDestino.setFoneFixo(tbOrigem.getFoneFixo());
        tbDestino.setEmail(tbOrigem.getEmail());
        tbDestino.setDtOpera(tbOrigem.getDtOpera());
    }
}
