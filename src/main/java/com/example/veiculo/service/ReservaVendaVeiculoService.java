package com.example.veiculo.service;

import com.example.veiculo.dto.ReervaVendaVeiculo.VeiculoMarcaDtoSaida;
import com.example.veiculo.dto.ReervaVendaVeiculo.VeiculoMarcaModeloDtoSaida;
import com.example.veiculo.dto.ReervaVendaVeiculo.VeiculoMarcaModeloVersaoDtoSaida;
import com.example.veiculo.dto.ReervaVendaVeiculo.VeiculoMarcaModeloVersaoQtdeEstoqueDtoSaida;
import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.model.ReservaVendaVeiculo;
import com.example.veiculo.repository.ReservaVendaVeiculoRepository;
import com.example.veiculo.repository.VeiculoRepository;
import com.example.veiculo.service.validator.ReservaVendaVeiculoNegocioValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReservaVendaVeiculoService {

    private final ReservaVendaVeiculoRepository reservaVendaVeiculoRepository;
    private final ReservaVendaVeiculoNegocioValidator veiculoNegocioValidator;
    private final VeiculoRepository veiculoRepository;
    private final PagamentoService pagamentoService;
    private final DocumentacaoRetiradaService documentacaoRetiradaService;

    public Optional<ReservaVendaVeiculo> obterReservaVendaVeiculoPorId(Long id) { return obterReservaVendaVeiculoPorId(id, false); }
    public Optional<ReservaVendaVeiculo> obterReservaVendaVeiculoPorId(Long id, boolean validaID) {
        Libs.isIdNull(id);
        var ret = reservaVendaVeiculoRepository.findById(id);
        if (!validaID) if (ret.isEmpty()) throw new RegistroNaoEncontradoException("Reserva Venda Veiculo com ID " + id + " não encontrada");
        return ret;
    }

    public Optional<List<ReservaVendaVeiculo>> listaVeiculosReservadosCanceladosVendidos() {
        return Optional.of(reservaVendaVeiculoRepository.findAll(Sort.by("valor")));
    }

    public Optional<List<ReservaVendaVeiculo>> listaVeiculosReservados() {
        return Optional.of(reservaVendaVeiculoRepository.findByStatus("R", Sort.by("valor")));
    }


    public Optional<List<ReservaVendaVeiculo>> listaVeiculosCancelados() {
        return Optional.of(reservaVendaVeiculoRepository.findByStatus("C", Sort.by("valor")));
    }

    public Optional<List<ReservaVendaVeiculo>> listaVeiculosVendidos() {
        return Optional.of(reservaVendaVeiculoRepository.findByStatus("V", Sort.by("valor")));
    }

    public Optional<List<VeiculoMarcaModeloVersaoDtoSaida>> listaVeiculoMarcaModeloVersao() {
        return Optional.of(reservaVendaVeiculoRepository.findVeiculoMarcaModeloVersao());
    }

    public Optional<List<VeiculoMarcaModeloVersaoQtdeEstoqueDtoSaida>> listaVeiculoMarcaModeloVersaoQtdeEstoque() {
        return Optional.of(reservaVendaVeiculoRepository.findVeiculoMarcaModeloVersaoQtdeEstoque());
    }

    public Optional<List<VeiculoMarcaModeloDtoSaida>> listaVeiculoMarcaModelo() {
        return Optional.of(reservaVendaVeiculoRepository.findVeiculoMarcaModelo());
    }


    public Optional<List<VeiculoMarcaDtoSaida>> listaVeiculoMarca() {
        return Optional.of(reservaVendaVeiculoRepository.findVeiculoMarca());
    }

    public Optional<List<ReservaVendaVeiculo>> listaVeiculos_Reservados() {
        return Optional.of(reservaVendaVeiculoRepository.findByStatusOrderByValorAsc("R", Sort.by("id")));
    }

    public Optional<List<ReservaVendaVeiculo>> listaVeiculos_Cancelados() {
        return Optional.of(reservaVendaVeiculoRepository.findByStatusOrderByValorAsc("C", Sort.by("id")));
    }

    public Optional<List<ReservaVendaVeiculo>> listaVeiculos_Vendidos() {
        return Optional.of(reservaVendaVeiculoRepository.findByStatusOrderByValorAsc("V", Sort.by("id")));
    }

    public Optional<List<ReservaVendaVeiculo>> listaVeiculos_Pendentes_Retirada() {
        return Optional.of(reservaVendaVeiculoRepository.findByStatusAndRetiradoOrderByValorAsc("V", "N", Sort.by("id")));
    }

    public Optional<List<ReservaVendaVeiculo>> listaVeiculos_Retirados() {
        return Optional.of(reservaVendaVeiculoRepository.findByStatusAndRetiradoOrderByValorAsc("V", "S", Sort.by("id")));
    }

    @Transactional
    public ReservaVendaVeiculo incluirReserva(ReservaVendaVeiculo tbEntrada) {
        veiculoNegocioValidator.validarNegocioInclusao(tbEntrada);
        tbEntrada.reservaVeiculo();
        tbEntrada.setRetirado("N");
        tbEntrada.setDtOpera(OffsetDateTime.now());
        tbEntrada.setDtReserva(tbEntrada.getDtOpera());
        var veiculo = veiculoRepository.findById(tbEntrada.getVeiculo().getId());
        veiculo.get().reservaVeiculo();
        var reservaSalva = reservaVendaVeiculoRepository.save(tbEntrada);
        pagamentoService.gerarParaReserva(reservaSalva);
        return reservaSalva;
    }

    @Transactional
    public void alterarReserva(ReservaVendaVeiculo tbInterna, ReservaVendaVeiculo tbEntrada) {
        veiculoNegocioValidator.validarNegocioAlteracao(tbEntrada);
        tbEntrada.setDtOpera(OffsetDateTime.now());
        copiarTabela(tbEntrada, tbInterna);
        reservaVendaVeiculoRepository.save(tbInterna);
    }

    @Transactional
    public boolean confirmaVenda(ReservaVendaVeiculo reservaVendaVeiculo) {
        veiculoNegocioValidator.validarNegocioConfirmaVenda(reservaVendaVeiculo);
        var dtOpera = OffsetDateTime.now();
        reservaVendaVeiculo.setDtVenda(dtOpera);
        reservaVendaVeiculo.setDtOpera(dtOpera);
        reservaVendaVeiculo.vendaVeiculo();
        reservaVendaVeiculo.getVeiculo().setDtOpera(dtOpera);
        reservaVendaVeiculo.getVeiculo().vendaVeiculo();
        return true;
    }

    @Transactional
    public boolean retiraVeiculo(ReservaVendaVeiculo reservaVendaVeiculo) {
        veiculoNegocioValidator.validarNegocioRetiraVeiculo(reservaVendaVeiculo);
        var dtOpera = OffsetDateTime.now();
        reservaVendaVeiculo.setDtRetirada(dtOpera);
        reservaVendaVeiculo.setDtOpera(dtOpera);
        reservaVendaVeiculo.retiradaVeiculo();
        documentacaoRetiradaService.emitir(reservaVendaVeiculo);
        return true;
    }

    @Transactional
    public boolean cancela(ReservaVendaVeiculo reservaVendaVeiculo) {
        veiculoNegocioValidator.validarNegocioExclusao(reservaVendaVeiculo);
        var dtOpera = OffsetDateTime.now();
        reservaVendaVeiculo.setDtCancelamento(dtOpera);
        reservaVendaVeiculo.setDtOpera(dtOpera);
        reservaVendaVeiculo.cancelaVeiculo();
        reservaVendaVeiculo.getVeiculo().setDtOpera(dtOpera);
        reservaVendaVeiculo.getVeiculo().ativaVeiculo();
        pagamentoService.cancelarPorReserva(reservaVendaVeiculo.getId());
        return true;
    }

    private void copiarTabela(ReservaVendaVeiculo tbOrigem, ReservaVendaVeiculo tbDestino) {
        tbDestino.setValor(tbOrigem.getValor());
        tbDestino.setVeiculo(tbOrigem.getVeiculo());
        tbDestino.setCliente(tbOrigem.getCliente());
        tbDestino.setDtOpera(tbOrigem.getDtOpera());
    }
}
