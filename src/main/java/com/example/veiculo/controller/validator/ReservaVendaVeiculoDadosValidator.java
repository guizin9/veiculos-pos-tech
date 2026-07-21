package com.example.veiculo.controller.validator;

import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.geral.config.exception.personal.RegistroDuplicadoException;
import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.model.ReservaVendaVeiculo;
import com.example.veiculo.repository.ReservaVendaVeiculoRepository;
import com.example.veiculo.service.validator.ReservaVendaVeiculoNegocioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReservaVendaVeiculoDadosValidator {
    private final ReservaVendaVeiculoNegocioValidator reservaVendaVeiculoNegocioValidator;
    private final ReservaVendaVeiculoRepository reservaVendaVeiculoRepository;

    public void validaId(Long id, Libs.TpOpe tpOpe) {
        if (tpOpe != Libs.TpOpe.INCLUSAO) {
            if (id == null) throw new RegistroNaoEncontradoException("O ID da reserva/venda do veículo não foi informado");
            if (!reservaVendaVeiculoRepository.existsById(id)) throw new RegistroDuplicadoException("O ID da versão informado não é válido. Possívelmente foi excluído por outro usuário!");
        } else {
            if (id != null) throw new RegistroNaoEncontradoException("O ID da reserva/venda não pode ser informado quando for inclusão de versão. ID informado: " + id.toString() + ".");
        }
    }

    public void validaDados(ReservaVendaVeiculo tbInterna, ReservaVendaVeiculo tbEntrada, Libs.TpOpe tpOpe) {
        validaId(tbEntrada.getId(), tpOpe);
        if (tpOpe == Libs.TpOpe.ALTERACAO) {
            if (tbEntrada.getVeiculo().getId() == null) throw new RegistroNaoEncontradoException("O ID do veículo não foi informado");
            if (tbEntrada.getCliente().getId() == null) throw new RegistroNaoEncontradoException("O ID do cliente não foi informado");
            reservaVendaVeiculoNegocioValidator.dadosAlterado(tbInterna, tbEntrada, true);
        }
    }
}
