package com.example.veiculo.service.validator;

import com.example.veiculo.geral.config.CpfUtil;
import com.example.veiculo.geral.config.exception.personal.CampoInvalidoException;
import com.example.veiculo.geral.config.exception.personal.ErroGeralException;
import com.example.veiculo.geral.config.exception.personal.RegistroDuplicadoException;
import com.example.veiculo.model.Cliente;
import com.example.veiculo.repository.ClienteRepository;
import com.example.veiculo.repository.ReservaVendaVeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class ClienteNegocioValidator {

    private final ClienteRepository clienteRepository;
    private final ReservaVendaVeiculoRepository reservaVendaVeiculoRepository;

    public void validarNegocioInclusao(Cliente tbEntrada) {
        validarCpf(tbEntrada.getCpf());
        if (clienteRepository.existsByNome(tbEntrada.getNome())) throw new RegistroDuplicadoException("Já existe um cliente cadastrado com o nome informado!");
        if (clienteRepository.existsByCpf(tbEntrada.getCpf() )) throw new RegistroDuplicadoException("Já existe um cliente cadastrado com o CPF informado!");
    }

    public void validarNegocioAlteracao(Cliente tbEntrada) {
//        validarDataOperacao(tbEntrada.getId(), tbEntrada.getDtOpera());
        validarCpf(tbEntrada.getCpf());
        if (clienteRepository.existsByNomeAndIdNot(tbEntrada.getNome(), tbEntrada.getId())) throw new RegistroDuplicadoException("Existe outro cliente cadastrado com o nome informado!");
        if (clienteRepository.existsByCpfAndIdNot (tbEntrada.getCpf() , tbEntrada.getId())) throw new RegistroDuplicadoException("Existe outro cliente cadastrado com o CPF informado!");
    }

    private void validarCpf(String cpf) {
        if (cpf != null && !cpf.isBlank() && !CpfUtil.isValido(cpf))
            throw new CampoInvalidoException("cpf", "CPF inválido");
    }

    public void validarNegocioExclusao(Long id) { validaSeUsadoPorOutraEntidade(id); }

//    public Cliente validarDataOperacao(Long id, OffsetDateTime dtOpera) {
//        var cliente = clienteRepository.findById(id);
//        if (cliente.isEmpty()) throw new RegistroDuplicadoException("O cliente " + cliente.get().getNome() + " com id " + id + " foi excluído por outro usuário!");
//        if (!Objects.equals(cliente.get().getDtOpera(), dtOpera)) throw new RegistroDuplicadoException("O cliente " + cliente.get().getNome() + " com id " + id +  " foi alterado por outro usuário!");
//        return cliente.get();
//    }

    public boolean dadosAlterado(Cliente tb, Cliente tblEntrada, boolean retornaThrow) {
        boolean igual = Objects.equals(tb.getNome()      , tblEntrada.getNome()      ) &&
                        Objects.equals(tb.getCpf()       , tblEntrada.getCpf()       ) &&
                        Objects.equals(tb.getLogradouro(), tblEntrada.getLogradouro()) &&
                        Objects.equals(tb.getNumero()    , tblEntrada.getNumero()    ) &&
                        Objects.equals(tb.getBairro()    , tblEntrada.getBairro()    ) &&
                        Objects.equals(tb.getCidade()    , tblEntrada.getCidade()    ) &&
                        Objects.equals(tb.getEstado()    , tblEntrada.getEstado()    ) &&
                        Objects.equals(tb.getCep()       , tblEntrada.getCep()       ) &&
                        Objects.equals(tb.getCelular()   , tblEntrada.getCelular()   ) &&
                        Objects.equals(tb.getFoneFixo()  , tblEntrada.getFoneFixo()  ) &&
                        Objects.equals(tb.getEmail()     , tblEntrada.getEmail()     ) ;
        if (igual && retornaThrow) throw new ErroGeralException("Não houve alteração de dados do cliente");
        return !igual;
    }

    private void validaSeUsadoPorOutraEntidade(Long clienteId) {
        if (reservaVendaVeiculoRepository.existsByClienteId(clienteId)) {
            var cliente = clienteRepository.findById(clienteId);
            throw new ErroGeralException("O cliente com o nome \"" + cliente.get().getNome() +  "\" não pode ser excluído porque possui uma ou mais registros de reserva associado a ele!");
        }
    }
}