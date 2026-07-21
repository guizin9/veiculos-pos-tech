package com.example.veiculo.controller.validator;

import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.geral.config.exception.personal.RegistroDuplicadoException;
import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.model.Marca;
import com.example.veiculo.repository.MarcaRepository;
import com.example.veiculo.service.validator.MarcaNegocioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MarcaDadosValidator {
    private final MarcaNegocioValidator marcaNegocioValidator;
    private final MarcaRepository marcaRepository;

    public void validaId(Long id, Libs.TpOpe tpOpe) {
        if (tpOpe != Libs.TpOpe.INCLUSAO) {
            if (id == null) throw new RegistroNaoEncontradoException("O ID da marca não foi informado");
            if (!marcaRepository.existsById(id)) throw new RegistroDuplicadoException("O ID da marca informado não é válido. Possívelmente foi excluído por outro usuário!");
        } else {
            if (id != null) throw new RegistroNaoEncontradoException("O ID da marca não pode ser informado quando for inclusão de marca. ID informado: " + id.toString() + ".");
        }
    }

    public void validaDados(Marca tbInterna, Marca tbEntrada, Libs.TpOpe tpOpe) {
        validaId(tbEntrada.getId(), tpOpe);
        if (tpOpe == Libs.TpOpe.ALTERACAO)
            marcaNegocioValidator.dadosAlterado(tbInterna, tbEntrada, true);
    }
}
