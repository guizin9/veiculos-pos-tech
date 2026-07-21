package com.example.veiculo.controller.validator;

import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.geral.config.exception.personal.RegistroDuplicadoException;
import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.model.Modelo;
import com.example.veiculo.repository.ModeloRepository;
import com.example.veiculo.service.validator.ModeloNegocioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ModeloDadosValidator {
    private final ModeloNegocioValidator modeloNegocioValidator;
    private final ModeloRepository modeloRepository;

    public void validaId(Long id, Libs.TpOpe tpOpe) {
        if (tpOpe != Libs.TpOpe.INCLUSAO) {
            if (id == null) throw new RegistroNaoEncontradoException("O ID do modelo não foi informado");
            if (!modeloRepository.existsById(id)) throw new RegistroDuplicadoException("O ID do modelo informado não é válido. Possívelmente foi excluído por outro usuário!");
        } else {
            if (id != null) throw new RegistroNaoEncontradoException("O ID do modelo não pode ser informado quando for inclusão de modelo. ID informado: " + id.toString() + ".");
        }
    }

    public void validaDados(Modelo tbInterna, Modelo tbEntrada, Libs.TpOpe tpOpe) {
        validaId(tbEntrada.getId(), tpOpe);
        if (tpOpe == Libs.TpOpe.ALTERACAO) {
            if (tbEntrada.getMarca().getId() == null) throw new RegistroNaoEncontradoException("O ID da marca não foi informado");
            modeloNegocioValidator.dadosAlterado(tbInterna, tbEntrada, true);
        }
    }
}
