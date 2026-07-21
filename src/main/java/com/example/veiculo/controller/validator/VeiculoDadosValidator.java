package com.example.veiculo.controller.validator;

import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.geral.config.exception.personal.RegistroDuplicadoException;
import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.model.Veiculo;
import com.example.veiculo.repository.VeiculoRepository;
import com.example.veiculo.service.validator.VeiculoNegocioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VeiculoDadosValidator {
    private final VeiculoNegocioValidator veiculoNegocioValidator;
    private final VeiculoRepository veiculoRepository;

    public void validaId(Long id, Libs.TpOpe tpOpe) {
        if (tpOpe != Libs.TpOpe.INCLUSAO) {
            if (id == null) throw new RegistroNaoEncontradoException("O ID do veículo não foi informado");
            if (!veiculoRepository.existsById(id)) throw new RegistroDuplicadoException("O ID da versão informado não é válido. Possívelmente foi excluído por outro usuário!");
        } else {
            if (id != null) throw new RegistroNaoEncontradoException("O ID do veículo não pode ser informado quando for inclusão de versão. ID informado: " + id.toString() + ".");
        }
    }

    public void validaDados(Veiculo tbInterna, Veiculo tbEntrada, Libs.TpOpe tpOpe) {
        validaId(tbEntrada.getId(), tpOpe);
        if (tpOpe == Libs.TpOpe.ALTERACAO) {
            if (tbEntrada.getVersao().getId() == null) throw new RegistroNaoEncontradoException("O ID da versão não foi informado");
            veiculoNegocioValidator.dadosAlterado(tbInterna, tbEntrada, true);
        }
    }
}
