package com.example.veiculo.service;

import com.example.veiculo.geral.config.exception.personal.ErroGeralException;
import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.model.Pagamento;
import com.example.veiculo.model.ReservaVendaVeiculo;
import com.example.veiculo.model.StatusPagamento;
import com.example.veiculo.model.Veiculo;
import com.example.veiculo.repository.PagamentoRepository;
import com.example.veiculo.repository.ReservaVendaVeiculoRepository;
import com.example.veiculo.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagamentoService {

    private final PagamentoRepository pagamentoRepository;
    private final ReservaVendaVeiculoRepository reservaVendaVeiculoRepository;
    private final VeiculoRepository veiculoRepository;

    @Value("${app.reserva.minutos-expiracao:30}")
    private long minutosExpiracao;

    public Optional<Pagamento> obterPorId(Long id) {
        return pagamentoRepository.findById(id);
    }

    public Pagamento obterPorCodigo(String codigo) {
        return pagamentoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Pagamento com código " + codigo + " não encontrado"));
    }

    public List<Pagamento> listar() {
        return pagamentoRepository.findAll();
    }

    public List<Pagamento> listarPorReserva(Long reservaId) {
        return pagamentoRepository.findByReservaId(reservaId);
    }

    /**
     * Gera o código de pagamento (fictício) para uma reserva ainda pendente.
     * Idempotente: se já existir um pagamento PENDENTE para a reserva, ele é retornado.
     */
    @Transactional
    public Pagamento gerarParaReserva(ReservaVendaVeiculo reserva) {
        var existente = pagamentoRepository.findFirstByReservaIdAndStatus(reserva.getId(), StatusPagamento.PENDENTE);
        if (existente.isPresent()) return existente.get();

        var agora = OffsetDateTime.now();
        Pagamento pagamento = new Pagamento();
        pagamento.setCodigo(gerarCodigo());
        pagamento.setReserva(reserva);
        pagamento.setValor(reserva.getValor());
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setDataGeracao(agora);
        pagamento.setDataExpiracao(agora.plusMinutes(minutosExpiracao));
        return pagamentoRepository.save(pagamento);
    }

    @Transactional
    public Pagamento gerarParaReservaId(Long reservaId) {
        var reserva = reservaVendaVeiculoRepository.findById(reservaId)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Reserva com ID " + reservaId + " não encontrada"));
        if (!"R".equals(reserva.getStatus()))
            throw new ErroGeralException("Só é possível gerar código de pagamento para reservas com status Reservado (R).");
        return gerarParaReserva(reserva);
    }

    @Transactional
    public Pagamento confirmarPagamento(String codigo) {
        return confirmarPagamento(obterPorCodigo(codigo));
    }

    @Transactional
    public Pagamento confirmarPagamento(Pagamento pagamento) {
        if (!pagamento.estaPendente())
            throw new ErroGeralException("Pagamento não está pendente. Status atual: " + pagamento.getStatus());
        if (pagamento.estaVencido()) {
            expirar(pagamento);
            throw new ErroGeralException("Pagamento expirado. Gere um novo código de pagamento.");
        }
        pagamento.pago();
        pagamento.setDataPagamento(OffsetDateTime.now());
        return pagamentoRepository.save(pagamento);
    }

    @Transactional
    public void cancelarPorReserva(Long reservaId) {
        for (Pagamento pagamento : pagamentoRepository.findByReservaId(reservaId)) {
            if (pagamento.estaPendente()) {
                pagamento.cancelado();
                pagamentoRepository.save(pagamento);
            }
        }
    }

    /**
     * Compensação por timeout: expira pagamentos vencidos, cancela a reserva
     * e libera o veículo (status A). Reutilizável por @Scheduled ou por um
     * gatilho externo (EventBridge / Lambda / Step Functions).
     */
    @Transactional
    public int expirarVencidos() {
        List<Pagamento> vencidos = pagamentoRepository
                .findByStatusAndDataExpiracaoBefore(StatusPagamento.PENDENTE, OffsetDateTime.now());
        for (Pagamento pagamento : vencidos) {
            expirar(pagamento);
        }
        return vencidos.size();
    }

    private void expirar(Pagamento pagamento) {
        var agora = OffsetDateTime.now();
        pagamento.expirado();
        pagamentoRepository.save(pagamento);

        ReservaVendaVeiculo reserva = pagamento.getReserva();
        if (reserva != null && "R".equals(reserva.getStatus())) {
            reserva.cancelaVeiculo();
            reserva.setDtCancelamento(agora);
            reserva.setDtOpera(agora);
            reservaVendaVeiculoRepository.save(reserva);

            if (reserva.getVeiculo() != null) {
                Optional<Veiculo> veiculo = veiculoRepository.findById(reserva.getVeiculo().getId());
                veiculo.ifPresent(v -> {
                    v.ativaVeiculo();
                    v.setDtOpera(agora);
                    veiculoRepository.save(v);
                });
            }
        }
    }

    private String gerarCodigo() {
        return "PAG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
