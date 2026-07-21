package com.example.veiculo.service.validator;

import com.example.veiculo.geral.config.exception.personal.ErroGeralException;
import com.example.veiculo.geral.config.exception.personal.RegistroDuplicadoException;
import com.example.veiculo.geral.config.exception.personal.RegistroSemIntegridadeException;
import com.example.veiculo.model.ReservaVendaVeiculo;
import com.example.veiculo.model.Veiculo;
import com.example.veiculo.repository.CorRepository;
import com.example.veiculo.repository.ReservaVendaVeiculoRepository;
import com.example.veiculo.repository.VeiculoRepository;
import com.example.veiculo.repository.VersaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class VeiculoNegocioValidator {

    private final VeiculoRepository veiculoRepository;
    private final VersaoRepository versaoRepository;
    private final CorRepository corRepository;
    private final ReservaVendaVeiculoRepository reservaVendaVeiculoRepository;

    public void validarNegocioInclusao(Veiculo tbEntrada) {
        if (veiculoRepository.existsByChassi(tbEntrada.getChassi())) throw new RegistroDuplicadoException("Já existe um veículo cadastrado com o número do chassi informado!");
        if (!corRepository.existsById(tbEntrada.getCor().getId())) throw new RegistroSemIntegridadeException("A cor informada é inválida!");
        if (!versaoRepository.existsById(tbEntrada.getVersao().getId())) throw new RegistroSemIntegridadeException("A versão informada é inválida!");
    }

    public void validarNegocioAlteracao(Veiculo tbEntrada) {
        if (veiculoRepository.existsByChassiAndIdNot(tbEntrada.getChassi(), tbEntrada.getId())) throw new RegistroDuplicadoException("Já existe um veículo cadastrado com o número do chassi informado!");
        if (!corRepository.existsById(tbEntrada.getCor().getId())) throw new RegistroSemIntegridadeException("A cor informada é inválida!");
        if (!versaoRepository.existsById(tbEntrada.getVersao().getId())) throw new RegistroSemIntegridadeException("A versão informada é inválida!");
    }

    public void validarNegocioExclusao(Long id) {
        validaSeUsadoPorOutraEntidade(id);
        var veiculo = veiculoRepository.findById(id);
        var status = veiculo.get().getStatus();
        if ( Objects.equals(status, "R")) throw new RegistroSemIntegridadeException("Veículo reservado não pode ser excluído!");
        if ( Objects.equals(status, "V")) throw new RegistroSemIntegridadeException("Veículo vendido não pode ser excluído!");
        if (!Objects.equals(status, "A")) throw new RegistroSemIntegridadeException("Só pode ser excluído veículo ativo!");
    }

//    public Veiculo validarDataOperacao(Long id, OffsetDateTime dtOpera) {
//        var veiculo = veiculoRepository.findById(id);
//        if (veiculo.isEmpty()) throw new RegistroDuplicadoException("O veículo com número de chassi " + veiculo.get().getChassi() + " e com id " + id + " foi excluído por outro usuário!");
//        if (!Objects.equals(veiculo.get().getDtOpera(), dtOpera)) throw new RegistroDuplicadoException("O veículo com o número de chassi " + veiculo.get().getChassi() + " e com id " + id +  " foi alterado por outro usuário!");
//        return veiculo.get();
//    }

    public boolean dadosAlterado(Veiculo tb, Veiculo tblEntrada, boolean retornaThrow) {
        boolean igual = Objects.equals(tb.getAnoFabricacao() , tblEntrada.getAnoFabricacao()  ) &&
                        Objects.equals(tb.getAnoModelo()     , tblEntrada.getAnoModelo()     ) &&
                        Objects.equals(tb.getChassi()        , tblEntrada.getChassi()        ) &&
                        Objects.equals(tb.getValor()         , tblEntrada.getValor()         ) &&
                        Objects.equals(tb.getCor().getId()   , tblEntrada.getCor().getId()   ) &&
                        Objects.equals(tb.getVersao().getId(), tblEntrada.getVersao().getId());
        if (igual && retornaThrow) throw new ErroGeralException("Não houve alteração de dados do veículo");
        return !igual;
    }

    private void validaSeUsadoPorOutraEntidade(Long veiculoId) {
        if (reservaVendaVeiculoRepository.existsByVeiculoId(veiculoId)) {
           var veiculo = veiculoRepository.findById(veiculoId);
           throw new ErroGeralException("O veículo com o chassi \"" + veiculo.get().getChassi() +  "\" não pode ser excluído porque possui uma ou mais registro de reserva associado a ele!");
        }
    }
}