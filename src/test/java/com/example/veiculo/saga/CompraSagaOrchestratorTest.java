package com.example.veiculo.saga;

import com.example.veiculo.geral.config.exception.personal.OperacaoNaoPemitidaExecption;
import com.example.veiculo.model.SagaCompra;
import com.example.veiculo.model.StatusPagamento;
import com.example.veiculo.repository.PagamentoRepository;
import com.example.veiculo.repository.SagaCompraRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompraSagaOrchestratorTest {

    @Mock
    private SagaCompraRepository sagaCompraRepository;
    @Mock
    private PagamentoRepository pagamentoRepository;

    @InjectMocks
    private CompraSagaOrchestrator orchestrator;

    private SagaCompra saga;

    @BeforeEach
    void setUp() {
        saga = new SagaCompra();
        saga.setId(1L);
        saga.setReservaId(10L);
        saga.setEtapa(SagaEtapa.PAGAMENTO_GERADO);
        saga.setStatus(SagaStatus.EM_ANDAMENTO);
        saga.setDtOperacao(OffsetDateTime.now());
    }

    @Test
    void validarPodeConfirmarVenda_semPagamento_deveLancarExcecao() {
        when(pagamentoRepository.existsByReservaIdAndStatus(10L, StatusPagamento.PAGO)).thenReturn(false);

        assertThrows(OperacaoNaoPemitidaExecption.class,
                () -> orchestrator.validarPodeConfirmarVenda(10L));
    }

    @Test
    void compensarCancelamento_idempotente_quandoJaConcluida() {
        saga.setStatus(SagaStatus.CONCLUIDA);
        when(sagaCompraRepository.findByReservaId(10L)).thenReturn(Optional.of(saga));

        orchestrator.compensarCancelamento(10L);

        verify(sagaCompraRepository, never()).save(any());
    }

    @Test
    void registrarPagamentoConfirmado_avancaEtapa() {
        when(sagaCompraRepository.findByReservaId(10L)).thenReturn(Optional.of(saga));
        when(sagaCompraRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        orchestrator.registrarPagamentoConfirmado(10L);

        verify(sagaCompraRepository).save(argThat(s ->
                s.getEtapa() == SagaEtapa.PAGAMENTO_CONFIRMADO));
    }
}
