package com.example.veiculo.service.validator;

import com.example.veiculo.geral.config.exception.personal.ErroGeralException;
import com.example.veiculo.geral.config.exception.personal.RegistroDuplicadoException;
import com.example.veiculo.geral.config.exception.personal.RegistroSemIntegridadeException;
import com.example.veiculo.model.Modelo;
//import com.example.veiculo.repository.VersaoRepository;
import com.example.veiculo.repository.MarcaRepository;
import com.example.veiculo.repository.ModeloRepository;
import com.example.veiculo.repository.VersaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ModeloNegocioValidator {

    private final ModeloRepository modeloRepository;
    private final VersaoRepository versaoRepository;
    private final MarcaRepository marcaRepository;

    public void validarNegocioInclusao(Modelo tbEntrada) {
        if (modeloRepository.existsByNome(tbEntrada.getNome())) throw new RegistroDuplicadoException("Já existe um modelo cadastrada com o nome informado!");
        if (!marcaRepository.existsById(tbEntrada.getMarca().getId())) throw new RegistroSemIntegridadeException("A marca informada é inválida!");
    }

    public void validarNegocioAlteracao(Modelo tbEntrada) {
        if (modeloRepository.existsByNomeAndIdNot(tbEntrada.getNome(), tbEntrada.getId())) throw new RegistroDuplicadoException("Já existe um modelo cadastrado com o nome informado!");
        if (!marcaRepository.existsById(tbEntrada.getMarca().getId())) throw new RegistroSemIntegridadeException("A marca informada é inválida!");
    }

    public void validarNegocioExclusao(Long id) { validaSeUsadoPorOutraEntidade(id); }

//    public Modelo validarDataOperacao(Long id, OffsetDateTime dtOpera) {
//        var modelo = modeloRepository.findById(id);
//        if (modelo.isEmpty()) throw new RegistroDuplicadoException("O modelo " + modelo.get().getNome() + " com id " + id + " foi excluído por outro usuário!");
//        if (!Objects.equals(modelo.get().getDtOpera(), dtOpera)) throw new RegistroDuplicadoException("O modelo " + modelo.get().getNome() + " com id " + id +  " foi alterado por outro usuário!");
//        return modelo.get();
//    }

    public boolean dadosAlterado(Modelo tb, Modelo tblEntrada, boolean retornaThrow) {
        boolean igual = Objects.equals(tb.getNome()         , tblEntrada.getNome()         ) &&
                        Objects.equals(tb.getMarca().getId(), tblEntrada.getMarca().getId());
        if (igual && retornaThrow) throw new ErroGeralException("Não houve alteração de dados do modelo");
        return !igual;
    }

    private void validaSeUsadoPorOutraEntidade(Long modeloId) {
        if (versaoRepository.existsByModeloId(modeloId)) {
           var modelo = modeloRepository.findById(modeloId);
            throw new ErroGeralException("O modelo com o nome \"" + modelo.get().getNome() +  "\" não pode ser excluído porque possui uma ou mais versões associadas a ele!");
        }
    }
}