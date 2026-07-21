package com.example.veiculo.controller.validator;

import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.geral.config.exception.personal.RegistroDuplicadoException;
import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.model.Cliente;
import com.example.veiculo.repository.ClienteRepository;
import com.example.veiculo.service.validator.ClienteNegocioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClienteDadosValidator {
    private final ClienteNegocioValidator marcaNegocioValidator;
    private final ClienteRepository marcaRepository;

    public void validaId(Long id, Libs.TpOpe tpOpe) {
        if (tpOpe != Libs.TpOpe.INCLUSAO) {
            if (id == null) throw new RegistroNaoEncontradoException("O ID do cliente não foi informado");
            if (!marcaRepository.existsById(id)) throw new RegistroDuplicadoException("O ID do cliente informado não é válido. Possívelmente foi excluído por outro usuário!");
        } else {
            if (id != null) throw new RegistroNaoEncontradoException("O ID do cliente não pode ser informado quando for inclusão de cliente. ID informado: " + id.toString() + ".");
        }
    }

    public void validaDados(Cliente tbInterna, Cliente tbEntrada, Libs.TpOpe tpOpe) {
        validaId(tbEntrada.getId(), tpOpe);
        if (tpOpe == Libs.TpOpe.ALTERACAO)
            marcaNegocioValidator.dadosAlterado(tbInterna, tbEntrada, true);
    }
}
