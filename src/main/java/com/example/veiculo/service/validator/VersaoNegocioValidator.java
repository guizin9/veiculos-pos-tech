package com.example.veiculo.service.validator;

import com.example.veiculo.geral.config.exception.personal.ErroGeralException;
import com.example.veiculo.geral.config.exception.personal.RegistroDuplicadoException;
import com.example.veiculo.geral.config.exception.personal.RegistroSemIntegridadeException;
import com.example.veiculo.model.Versao;
import com.example.veiculo.repository.ModeloRepository;
import com.example.veiculo.repository.VeiculoRepository;
import com.example.veiculo.repository.VersaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class VersaoNegocioValidator {

    private final VersaoRepository versaoRepository;
    private final ModeloRepository modeloRepository;
    private final VeiculoRepository veiculoRepository;

    public void validarNegocioInclusao(Versao tbEntrada) {
        if (versaoRepository.existsByNome(tbEntrada.getNome())) throw new RegistroDuplicadoException("Já existe uma verão cadastrada com o nome informado!");
        if (!modeloRepository.existsById(tbEntrada.getModelo().getId())) throw new RegistroSemIntegridadeException("O modelo informado é inválido!");
    }

    public void validarNegocioAlteracao(Versao tbEntrada) {
//        validarDataOperacao(tbEntrada.getId(), tbEntrada.getDtOpera());
        if (versaoRepository.existsByNomeAndIdNot(tbEntrada.getNome(), tbEntrada.getId())) throw new RegistroDuplicadoException("Já existe um versão cadastrada com o nome informado!");
        if (!modeloRepository.existsById(tbEntrada.getModelo().getId())) throw new RegistroSemIntegridadeException("O modelo informado é inválido!");
    }

    public void validarNegocioExclusao(Long id) { validaSeUsadoPorOutraEntidade(id); }

//    public Versao validarDataOperacao(Long id, OffsetDateTime dtOpera) {
//        var versao = versaoRepository.findById(id);
//        if (versao.isEmpty()) throw new RegistroDuplicadoException("A versão " + versao.get().getNome() + " com id " + id + " foi excluído por outro usuário!");
//        if (!Objects.equals(versao.get().getDtOpera(), dtOpera)) throw new RegistroDuplicadoException("A versão " + versao.get().getNome() + " com id " + id +  " foi alterado por outro usuário!");
//        return versao.get();
//    }

    public boolean dadosAlterado(Versao tb, Versao tblEntrada, boolean retornaThrow) {
        boolean igual = Objects.equals(tb.getNome()          , tblEntrada.getNome()          ) &&
                        Objects.equals(tb.getModelo().getId(), tblEntrada.getModelo().getId());
        if (igual && retornaThrow) throw new ErroGeralException("Não houve alteração de dados do versao");
        return !igual;
    }

    private void validaSeUsadoPorOutraEntidade(Long versaoId) {
        if (veiculoRepository.existsByVersaoId(versaoId)) {
            var versao = versaoRepository.findById(versaoId);
            throw new ErroGeralException("O versão com o nome \"" + versao.get().getNome() +  "\" não pode ser excluído porque possui uma ou mais veículos associados a ela!");
        }
    }
}