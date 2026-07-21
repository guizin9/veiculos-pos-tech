package com.example.veiculo.service.validator;

import com.example.veiculo.geral.config.exception.personal.ErroGeralException;
import com.example.veiculo.geral.config.exception.personal.RegistroDuplicadoException;
import com.example.veiculo.model.Cor;
import com.example.veiculo.repository.CorRepository;
import com.example.veiculo.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class CorNegocioValidator {

    private final CorRepository corRepository;
    private final VeiculoRepository veiculoRepository;

    public void validarNegocioInclusao(Cor tbEntrada) {
        if (corRepository.existsByNome(tbEntrada.getNome())) throw new RegistroDuplicadoException("Já existe uma cor cadastrada com o nome informado!");
    }

    public void validarNegocioAlteracao(Cor tbEntrada) {
//        validarDataOperacao(tbEntrada.getId(), tbEntrada.getDtOpera());
        if (corRepository.existsByNomeAndIdNot(tbEntrada.getNome(), tbEntrada.getId())) throw new RegistroDuplicadoException("Já existe uma cor cadastrada com o nome informado!");
    }

    public void validarNegocioExclusao(Long id) { validaSeUsadoPorOutraEntidade(id); }

//    public Cor validarDataOperacao(Long id, OffsetDateTime dtOpera) {
//        var cor = corRepository.findById(id);
//        if (cor.isEmpty()) throw new RegistroDuplicadoException("A cor " + cor.get().getNome() + " com id " + id + " foi excluída por outro usuário!");
//        if (!Objects.equals(cor.get().getDtOpera(), dtOpera)) throw new RegistroDuplicadoException("A cor " + cor.get().getNome() + " com id " + id +  " foi alterada por outro usuário!");
//        return cor.get();
//    }

    public boolean dadosAlterado(Cor tb, Cor tblEntrada, boolean retornaThrow) {
        boolean igual = Objects.equals(tb.getNome()  , tblEntrada.getNome());
        if (igual && retornaThrow) throw new ErroGeralException("Não houve alteração de dados da cor");
        return !igual;
    }

    private void validaSeUsadoPorOutraEntidade(Long corId) {
        if (veiculoRepository.existsByCorId(corId)) {
           var cor = corRepository.findById(corId);
           throw new ErroGeralException("A cor com o nome \"" + cor.get().getNome() +  "\" não pode ser excluída porque possui uma ou mais veículos associados a ela!");
        }
    }
}