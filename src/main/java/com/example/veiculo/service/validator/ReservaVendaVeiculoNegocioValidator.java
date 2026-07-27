package com.example.veiculo.service.validator;

import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.geral.config.exception.personal.ErroGeralException;
import com.example.veiculo.geral.config.exception.personal.OperacaoNaoPemitidaExecption;
import com.example.veiculo.geral.config.exception.personal.RegistroDuplicadoException;
import com.example.veiculo.geral.config.exception.personal.RegistroSemIntegridadeException;
import com.example.veiculo.model.ReservaVendaVeiculo;
import com.example.veiculo.repository.ClienteRepository;
import com.example.veiculo.repository.ReservaVendaVeiculoRepository;
import com.example.veiculo.repository.VeiculoRepository;
import com.example.veiculo.repository.VersaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ReservaVendaVeiculoNegocioValidator {

    private final ReservaVendaVeiculoRepository reservaVendaVeiculoRepository;
    private final VeiculoRepository veiculoRepository;
    private final ClienteRepository clienteRepository;

    public void validarNegocioInclusao(ReservaVendaVeiculo tbEntrada) {
       if (!veiculoRepository.existsById(tbEntrada.getVeiculo().getId())) throw new RegistroSemIntegridadeException("O veículo informado é inválido!");
       var cliente = clienteRepository.findById(tbEntrada.getCliente().getId())
               .orElseThrow(() -> new RegistroSemIntegridadeException("O cliente informado é inválido!"));
       if (!cliente.isAtivo()) throw new OperacaoNaoPemitidaExecption("O cliente informado não está ativo. A venda só é permitida para compradores cadastrados e ativos.");
       if (!veiculoRepository.existsByIdAndValor(tbEntrada.getVeiculo().getId(), tbEntrada.getValor())) throw new RegistroSemIntegridadeException("O valor informado está divergente do valor cadastrado para o veículo!");
       if (reservaVendaVeiculoRepository.existsByVeiculoIdAndStatus(tbEntrada.getVeiculo().getId(), "R")) throw new RegistroDuplicadoException("Já existe um Reserva cadastrada com o veículo informado!");
       if (reservaVendaVeiculoRepository.existsByVeiculoIdAndStatus(tbEntrada.getVeiculo().getId(), "V")) throw new RegistroDuplicadoException("Já existe um Venda realizada com o veículo informado!");
    }

    public void validarNegocioAlteracao(ReservaVendaVeiculo tbEntrada) {
//        validarDataOperacao(tbEntrada.getId(), tbEntrada.getDtOpera());
        if (!veiculoRepository.existsById(tbEntrada.getVeiculo().getId())) throw new RegistroSemIntegridadeException("O veículo informado é inválido!");
        if (!clienteRepository.existsById(tbEntrada.getCliente().getId())) throw new RegistroSemIntegridadeException("O cliente informado é inválido!");
        if (!veiculoRepository.existsByIdAndValor(tbEntrada.getVeiculo().getId(), tbEntrada.getValor())) throw new RegistroSemIntegridadeException("O valor informado está divergente do valor cadastrado para o veículo!");
        if (reservaVendaVeiculoRepository.existsByVeiculoIdAndStatusAndIdNot(tbEntrada.getVeiculo().getId(), "R", tbEntrada.getId())) throw new RegistroDuplicadoException("Já existe um Reserva cadastrada com o veículo informado!");
        if (reservaVendaVeiculoRepository.existsByVeiculoIdAndStatusAndIdNot(tbEntrada.getVeiculo().getId(), "V", tbEntrada.getId())) throw new RegistroDuplicadoException("Já existe um Venda realizada com o veículo informado!");
        validaStatus(tbEntrada, Libs.TpOpe.ALTERACAO);
    }

    public void validarNegocioExclusao     (ReservaVendaVeiculo reservaVendaVeiculo) { validaStatus(reservaVendaVeiculo, Libs.TpOpe.EXCLUSAO   ); }
    public void validarNegocioConfirmaVenda(ReservaVendaVeiculo reservaVendaVeiculo) { validaStatus(reservaVendaVeiculo, Libs.TpOpe.CONFIRMACAO); }
    public void validarNegocioRetiraVeiculo(ReservaVendaVeiculo reservaVendaVeiculo) { validaStatus(reservaVendaVeiculo, Libs.TpOpe.EXIT);        }

    private void validaStatus(ReservaVendaVeiculo reservaVendaVeiculo, Libs.TpOpe tpOpe) {
        var id     = reservaVendaVeiculo.getId();
        var status = reservaVendaVeiculo.getStatus();

        if (tpOpe == Libs.TpOpe.EXIT) {
            if (!Objects.equals(status, "V")) {
                throw new ErroGeralException("A venda de veículo com código de Reserva/Venda " + id + " não pode ser entregue para o cliente porque está com status " +
                        (Objects.equals(status, "C") ? "cancelada!" :
                        (Objects.equals(status, "R") ? "Reservada!" : "") + ". Só é possível fazer retirada de veículo com status de Vendido."));
            }
            if (reservaVendaVeiculo.getRetirado().equals("S")) throw new ErroGeralException("A venda de veículo com id " + id + " já foi retirada pelo cliente");
        }
        else if (!Objects.equals(status, "R")) {
            var msg = tpOpe == Libs.TpOpe.ALTERACAO   ? "alterada" :
                      tpOpe == Libs.TpOpe.EXCLUSAO    ? "cancelada" :
                      tpOpe == Libs.TpOpe.CONFIRMACAO ? "confirmada a venda" : "";
            throw new ErroGeralException("A Reserva/Venda com id " + id + " não pode ser " + msg + " porque foi " +
                    (Objects.equals(status, "C") ? "cancelada!" :
                    (Objects.equals(status, "V") ? "confirmada a venda!" : "")));
        }
    }

    public boolean dadosAlterado(ReservaVendaVeiculo tb, ReservaVendaVeiculo tblEntrada, boolean retornaThrow) {
        boolean igual = Objects.equals(tb.getStatus()         , tblEntrada.getStatus()         ) &&
                        Objects.equals(tb.getValor()          , tblEntrada.getValor()          ) &&
                        Objects.equals(tb.getVeiculo().getId(), tblEntrada.getVeiculo().getId()) &&
                        Objects.equals(tb.getCliente().getId(), tblEntrada.getCliente().getId());
        if (igual && retornaThrow) throw new ErroGeralException("Não houve alteração de dados da Reserva/Venda");
        return !igual;
    }
}