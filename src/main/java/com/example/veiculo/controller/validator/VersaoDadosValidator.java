package com.example.veiculo.controller.validator;

import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.geral.config.exception.personal.RegistroDuplicadoException;
import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.model.Versao;
import com.example.veiculo.repository.VersaoRepository;
import com.example.veiculo.service.validator.VersaoNegocioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VersaoDadosValidator {
    private final VersaoNegocioValidator versaoNegocioValidator;
    private final VersaoRepository versaoRepository;

    public void validaId(Long id, Libs.TpOpe tpOpe) {
        if (tpOpe != Libs.TpOpe.INCLUSAO) {
            if (id == null) throw new RegistroNaoEncontradoException("O ID da versão não foi informado");
            if (!versaoRepository.existsById(id)) throw new RegistroDuplicadoException("O ID da versão informado não é válido. Possívelmente foi excluído por outro usuário!");
        } else {
            if (id != null) throw new RegistroNaoEncontradoException("O ID da versão não pode ser informado quando for inclusão de versão. ID informado: " + id.toString() + ".");
        }
    }

    public void validaDados(Versao tbInterna, Versao tbEntrada, Libs.TpOpe tpOpe) {
        validaId(tbEntrada.getId(), tpOpe);
        if (tpOpe == Libs.TpOpe.ALTERACAO) {
            if (tbEntrada.getModelo().getId() == null) throw new RegistroNaoEncontradoException("O ID do modelo não foi informado");
            versaoNegocioValidator.dadosAlterado(tbInterna, tbEntrada, true);
        }
    }
}
