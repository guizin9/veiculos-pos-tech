package com.example.veiculo.controller.validator;

import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.geral.config.exception.personal.ErroGeralException;
import com.example.veiculo.geral.config.exception.personal.RegistroDuplicadoException;
import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.geral.config.exception.personal.RegistroNaoEnviadoException;
import com.example.veiculo.model.Cor;
import com.example.veiculo.repository.CorRepository;
import com.example.veiculo.service.validator.CorNegocioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class CorDadosValidator {
    private final CorNegocioValidator corNegocioValidator;
    private final CorRepository corRepository;

    public void validaId(Long id, Libs.TpOpe tpOpe) {
        if (tpOpe != Libs.TpOpe.INCLUSAO) {
            if (id == null) throw new RegistroNaoEncontradoException("O ID da cor não foi informado");
            if (!corRepository.existsById(id)) throw new RegistroDuplicadoException("O ID da cor informado não é válido. Possívelmente foi excluído por outro usuário!");
        } else {
            if (id != null) throw new RegistroNaoEncontradoException("O ID da cor não pode ser informado quando for inclusão de cor. ID informado: " + id.toString() + ".");
        }
    }

    public void validaDados(Cor tbInterna, Cor tbEntrada, Libs.TpOpe tpOpe) {
        validaId(tbEntrada.getId(), tpOpe);
         if (tpOpe == Libs.TpOpe.ALTERACAO)
             corNegocioValidator.dadosAlterado(tbInterna, tbEntrada, true);
    }
}
