package com.example.veiculo.service.validator;

import com.example.veiculo.geral.config.exception.personal.ErroGeralException;
import com.example.veiculo.geral.config.exception.personal.RegistroDuplicadoException;
import com.example.veiculo.model.Marca;
import com.example.veiculo.repository.MarcaRepository;
//import com.example.veiculo.repository.ModeloRepository;
import com.example.veiculo.repository.ModeloRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class MarcaNegocioValidator {

    private final MarcaRepository marcaRepository;
    private final ModeloRepository modeloRepository;

    public void validarNegocioInclusao(Marca tbEntrada) {
        if (marcaRepository.existsByNome(tbEntrada.getNome())) throw new RegistroDuplicadoException("Já existe uma marca cadastrada com o nome informado!");
    }

    public void validarNegocioAlteracao(Marca tbEntrada) {
//        validarDataOperacao(tbEntrada.getId(), tbEntrada.getDtOpera());
        if (marcaRepository.existsByNomeAndIdNot(tbEntrada.getNome(), tbEntrada.getId())) throw new RegistroDuplicadoException("Já existe uma marca cadastrada com o nome informado!");
    }

    public void validarNegocioExclusao(Long id) { validaSeUsadoPorOutraEntidade(id); }

//    public Marca validarDataOperacao(Long id, OffsetDateTime dtOpera) {
//        var marca = marcaRepository.findById(id);
//        if (marca.isEmpty()) throw new RegistroDuplicadoException("A marca " + marca.get().getNome() + " com id " + id + " foi excluída por outro usuário!");
//        if (!Objects.equals(marca.get().getDtOpera(), dtOpera)) throw new RegistroDuplicadoException("A marca " + marca.get().getNome() + " com id " + id +  " foi alterada por outro usuário!");
//        return marca.get();
//    }

    public boolean dadosAlterado(Marca tb, Marca tblEntrada, boolean retornaThrow) {
        boolean igual = Objects.equals(tb.getNome()  , tblEntrada.getNome());
        if (igual && retornaThrow) throw new ErroGeralException("Não houve alteração de dados da marca");
        return !igual;
    }

    private void validaSeUsadoPorOutraEntidade(Long marcaId) {
        if (modeloRepository.existsByMarcaId(marcaId)) {
           var marca = marcaRepository.findById(marcaId);
           throw new ErroGeralException("A marca com o nome \"" + marca.get().getNome() +  "\" não pode ser excluída porque possui uma ou mais modelos associados a ela!");
        }
    }
}