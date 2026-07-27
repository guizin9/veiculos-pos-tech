package com.example.veiculo.saga;

import com.example.veiculo.geral.config.exception.personal.ErroGeralException;
import com.example.veiculo.geral.config.exception.personal.OperacaoNaoPemitidaExecption;
import com.example.veiculo.geral.logging.SagaLoggingContext;
import com.example.veiculo.model.SagaCompra;
import com.example.veiculo.model.StatusPagamento;
import com.example.veiculo.repository.PagamentoRepository;
import com.example.veiculo.repository.SagaCompraRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

/**
 * Orquestrador interno da SAGA de compra (monólito).
 * Controla etapas, valida transições, garante idempotência e registra compensações.
 */
@Component
@RequiredArgsConstructor
public class CompraSagaOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(CompraSagaOrchestrator.class);

    private final SagaCompraRepository sagaCompraRepository;
    private final PagamentoRepository pagamentoRepository;

    @Transactional
    public SagaCompra iniciar(Long reservaId) {
        SagaLoggingContext.bindReserva(reservaId);
        return sagaCompraRepository.findByReservaId(reservaId)
                .orElseGet(() -> {
                    SagaCompra saga = new SagaCompra();
                    saga.setReservaId(reservaId);
                    saga.setEtapa(SagaEtapa.RESERVA);
                    saga.setStatus(SagaStatus.EM_ANDAMENTO);
                    saga.setDtOperacao(OffsetDateTime.now());
                    SagaCompra salva = sagaCompraRepository.save(saga);
                    log.info("SAGA iniciada reservaId={} etapa={}", reservaId, SagaEtapa.RESERVA);
                    return salva;
                });
    }

    @Transactional
    public void registrarPagamentoGerado(Long reservaId) {
        avancar(reservaId, SagaEtapa.PAGAMENTO_GERADO);
    }

    @Transactional
    public void registrarPagamentoConfirmado(Long reservaId) {
        avancar(reservaId, SagaEtapa.PAGAMENTO_CONFIRMADO);
    }

    /** Exige pagamento PAGO antes de confirmar a venda. */
    public void validarPodeConfirmarVenda(Long reservaId) {
        if (!pagamentoRepository.existsByReservaIdAndStatus(reservaId, StatusPagamento.PAGO))
            throw new OperacaoNaoPemitidaExecption(
                    "Não é possível confirmar a venda: o pagamento ainda não foi confirmado para a reserva " + reservaId + ".");
        SagaCompra saga = obter(reservaId);
        if (saga.getEtapa() != SagaEtapa.PAGAMENTO_CONFIRMADO && saga.getEtapa() != SagaEtapa.VENDA)
            throw new ErroGeralException("SAGA: etapa atual (" + saga.getEtapa() + ") não permite confirmar venda.");
    }

    @Transactional
    public void registrarVenda(Long reservaId) {
        avancar(reservaId, SagaEtapa.VENDA);
    }

    @Transactional
    public void registrarDocumentacao(Long reservaId) {
        avancar(reservaId, SagaEtapa.DOCUMENTACAO);
    }

    @Transactional
    public void registrarRetirada(Long reservaId) {
        SagaLoggingContext.bindReserva(reservaId);
        SagaCompra saga = obter(reservaId);
        saga.setEtapa(SagaEtapa.RETIRADA);
        saga.setStatus(SagaStatus.CONCLUIDA);
        saga.setDtOperacao(OffsetDateTime.now());
        sagaCompraRepository.save(saga);
        log.info("SAGA concluída reservaId={} etapa={}", reservaId, SagaEtapa.RETIRADA);
    }

    /** Compensação: cancelamento manual ou falha — libera fluxo. */
    @Transactional
    public void compensarCancelamento(Long reservaId) {
        SagaLoggingContext.bindReserva(reservaId);
        SagaCompra saga = obter(reservaId);
        if (saga.getStatus() == SagaStatus.CONCLUIDA) return; // idempotente
        saga.setEtapa(SagaEtapa.CANCELADA);
        saga.setStatus(SagaStatus.COMPENSADA);
        saga.setDtOperacao(OffsetDateTime.now());
        sagaCompraRepository.save(saga);
        log.info("SAGA compensada reservaId={} etapa={}", reservaId, SagaEtapa.CANCELADA);
    }

    /** Compensação: pagamento expirado / timeout. */
    @Transactional
    public void compensarExpiracao(Long reservaId) {
        SagaLoggingContext.bindReserva(reservaId);
        SagaCompra saga = sagaCompraRepository.findByReservaId(reservaId).orElse(null);
        if (saga == null || saga.getStatus() == SagaStatus.CONCLUIDA) return;
        saga.setEtapa(SagaEtapa.EXPIRADA);
        saga.setStatus(SagaStatus.COMPENSADA);
        saga.setDtOperacao(OffsetDateTime.now());
        sagaCompraRepository.save(saga);
        log.info("SAGA compensada reservaId={} etapa={}", reservaId, SagaEtapa.EXPIRADA);
    }

    public SagaCompra obter(Long reservaId) {
        return sagaCompraRepository.findByReservaId(reservaId)
                .orElseThrow(() -> new ErroGeralException("SAGA não encontrada para reserva " + reservaId));
    }

    private void avancar(Long reservaId, SagaEtapa novaEtapa) {
        SagaLoggingContext.bindReserva(reservaId);
        SagaCompra saga = sagaCompraRepository.findByReservaId(reservaId)
                .orElseThrow(() -> new ErroGeralException("SAGA não iniciada para reserva " + reservaId));
        if (saga.getStatus() == SagaStatus.COMPENSADA || saga.getStatus() == SagaStatus.CONCLUIDA) return;
        // Idempotência: se já está na etapa ou além, não retrocede
        if (saga.getEtapa().ordinal() >= novaEtapa.ordinal()) return;
        saga.setEtapa(novaEtapa);
        saga.setDtOperacao(OffsetDateTime.now());
        sagaCompraRepository.save(saga);
        log.info("SAGA avanço reservaId={} etapa={}", reservaId, novaEtapa);
    }
}
