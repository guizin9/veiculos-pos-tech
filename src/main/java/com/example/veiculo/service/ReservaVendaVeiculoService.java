package com.example.veiculo.service;

import com.example.veiculo.dto.ReervaVendaVeiculo.VeiculoMarcaDtoSaida;
import com.example.veiculo.dto.ReervaVendaVeiculo.VeiculoMarcaModeloDtoSaida;
import com.example.veiculo.dto.ReervaVendaVeiculo.VeiculoMarcaModeloVersaoDtoSaida;
import com.example.veiculo.dto.ReervaVendaVeiculo.VeiculoMarcaModeloVersaoQtdeEstoqueDtoSaida;
import com.example.veiculo.geral.config.Libs;
import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.messaging.SagaEvent;
import com.example.veiculo.messaging.SagaEventBus;
import com.example.veiculo.messaging.SagaEventType;
import com.example.veiculo.model.ReservaVendaVeiculo;
import com.example.veiculo.repository.ReservaVendaVeiculoRepository;
import com.example.veiculo.repository.VeiculoRepository;
import com.example.veiculo.saga.CompraSagaOrchestrator;
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
    private final SagaEventBus sagaEventBus;
    private final CompraSagaOrchestrator sagaOrchestrator;

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
        var veiculo = veiculoRepository.findById(tbEntrada.getVeiculo().getId()).orElseThrow();
        veiculo.reservaVeiculo();
        veiculo.setDtOpera(tbEntrada.getDtOpera());
        veiculoRepository.save(veiculo);
        var reservaSalva = reservaVendaVeiculoRepository.save(tbEntrada);
        sagaOrchestrator.iniciar(reservaSalva.getId());
        var pagamento = pagamentoService.gerarParaReserva(reservaSalva);
        sagaOrchestrator.registrarPagamentoGerado(reservaSalva.getId());
        sagaEventBus.publicar(SagaEvent.of(SagaEventType.RESERVA_CRIADA,
                reservaSalva.getId(), reservaSalva.getCliente().getId(), reservaSalva.getVeiculo().getId()));
        sagaEventBus.publicar(SagaEvent.of(SagaEventType.PAGAMENTO_GERADO,
                reservaSalva.getId(), reservaSalva.getCliente().getId(), reservaSalva.getVeiculo().getId(),
                pagamento.getCodigo()));
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
        sagaOrchestrator.validarPodeConfirmarVenda(reservaVendaVeiculo.getId());
        var dtOpera = OffsetDateTime.now();
        reservaVendaVeiculo.setDtVenda(dtOpera);
        reservaVendaVeiculo.setDtOpera(dtOpera);
        reservaVendaVeiculo.vendaVeiculo();
        var veiculo = veiculoRepository.findById(reservaVendaVeiculo.getVeiculo().getId()).orElseThrow();
        veiculo.setDtOpera(dtOpera);
        veiculo.vendaVeiculo();
        veiculoRepository.save(veiculo);
        reservaVendaVeiculoRepository.save(reservaVendaVeiculo);
        sagaOrchestrator.registrarVenda(reservaVendaVeiculo.getId());
        sagaEventBus.publicar(SagaEvent.of(SagaEventType.VENDA_CONFIRMADA,
                reservaVendaVeiculo.getId(), reservaVendaVeiculo.getCliente().getId(),
                reservaVendaVeiculo.getVeiculo().getId()));
        return true;
    }

    @Transactional
    public boolean retiraVeiculo(ReservaVendaVeiculo reservaVendaVeiculo) {
        veiculoNegocioValidator.validarNegocioRetiraVeiculo(reservaVendaVeiculo);
        var dtOpera = OffsetDateTime.now();
        reservaVendaVeiculo.setDtRetirada(dtOpera);
        reservaVendaVeiculo.setDtOpera(dtOpera);
        reservaVendaVeiculo.retiradaVeiculo();
        reservaVendaVeiculoRepository.save(reservaVendaVeiculo);
        documentacaoRetiradaService.emitir(reservaVendaVeiculo);
        sagaOrchestrator.registrarDocumentacao(reservaVendaVeiculo.getId());
        sagaOrchestrator.registrarRetirada(reservaVendaVeiculo.getId());
        sagaEventBus.publicar(SagaEvent.of(SagaEventType.DOCUMENTACAO_EMITIDA,
                reservaVendaVeiculo.getId(), reservaVendaVeiculo.getCliente().getId(),
                reservaVendaVeiculo.getVeiculo().getId()));
        return true;
    }

    @Transactional
    public boolean cancela(ReservaVendaVeiculo reservaVendaVeiculo) {
        veiculoNegocioValidator.validarNegocioExclusao(reservaVendaVeiculo);
        var dtOpera = OffsetDateTime.now();
        reservaVendaVeiculo.setDtCancelamento(dtOpera);
        reservaVendaVeiculo.setDtOpera(dtOpera);
        reservaVendaVeiculo.cancelaVeiculo();
        var veiculo = veiculoRepository.findById(reservaVendaVeiculo.getVeiculo().getId()).orElseThrow();
        veiculo.setDtOpera(dtOpera);
        veiculo.ativaVeiculo();
        veiculoRepository.save(veiculo);
        reservaVendaVeiculoRepository.save(reservaVendaVeiculo);
        pagamentoService.cancelarPorReserva(reservaVendaVeiculo.getId());
        sagaOrchestrator.compensarCancelamento(reservaVendaVeiculo.getId());
        sagaEventBus.publicar(SagaEvent.of(SagaEventType.RESERVA_CANCELADA,
                reservaVendaVeiculo.getId(), reservaVendaVeiculo.getCliente().getId(),
                reservaVendaVeiculo.getVeiculo().getId()));
        return true;
    }

    private void copiarTabela(ReservaVendaVeiculo tbOrigem, ReservaVendaVeiculo tbDestino) {
        tbDestino.setValor(tbOrigem.getValor());
        tbDestino.setVeiculo(tbOrigem.getVeiculo());
        tbDestino.setCliente(tbOrigem.getCliente());
        tbDestino.setDtOpera(tbOrigem.getDtOpera());
    }
}
