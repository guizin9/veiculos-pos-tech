package com.example.veiculo.service;

import com.example.veiculo.model.*;
import com.example.veiculo.repository.*;
import com.example.veiculo.saga.SagaEtapa;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CompraFluxoIntegrationTest {

    @Autowired private MarcaRepository marcaRepository;
    @Autowired private ModeloRepository modeloRepository;
    @Autowired private VersaoRepository versaoRepository;
    @Autowired private CorRepository corRepository;
    @Autowired private VeiculoRepository veiculoRepository;
    @Autowired private ClienteRepository clienteRepository;
    @Autowired private ReservaVendaVeiculoService reservaService;
    @Autowired private PagamentoService pagamentoService;
    @Autowired private PagamentoRepository pagamentoRepository;
    @Autowired private SagaCompraRepository sagaCompraRepository;
    @Autowired private DocumentacaoRetiradaRepository documentacaoRetiradaRepository;

    private Long veiculoId;
    private Long clienteId;

    @BeforeEach
    void seed() {
        var marca = marcaRepository.save(createMarca());
        var modelo = new Modelo();
        modelo.setNome("Modelo Teste");
        modelo.setMarca(marca);
        modelo = modeloRepository.save(modelo);
        var versao = new Versao();
        versao.setNome("Versao 1.0");
        versao.setModelo(modelo);
        versao = versaoRepository.save(versao);
        var cor = corRepository.save(createCor());

        var veiculo = new Veiculo();
        veiculo.setStatus("A");
        veiculo.setAnoFabricacao((short) 2023);
        veiculo.setAnoModelo((short) 2024);
        veiculo.setChassi("9BWZZZ377VT004251");
        veiculo.setValor(new BigDecimal("50000.00"));
        veiculo.setVersao(versao);
        veiculo.setCor(cor);
        veiculoId = veiculoRepository.save(veiculo).getId();

        var cliente = new Cliente();
        cliente.setNome("Cliente Teste");
        cliente.setCpf("52998224725");
        cliente.setAtivo(true);
        clienteId = clienteRepository.save(cliente).getId();
    }

    @Test
    void fluxoCompleto_reserva_pagamento_venda_retirada() {
        var reserva = new ReservaVendaVeiculo();
        reserva.setValor(new BigDecimal("50000.00"));
        var v = new Veiculo();
        v.setId(veiculoId);
        reserva.setVeiculo(v);
        var c = new Cliente();
        c.setId(clienteId);
        reserva.setCliente(c);

        var salva = reservaService.incluirReserva(reserva);
        assertEquals("R", salva.getStatus());

        var pagamentos = pagamentoRepository.findByReservaId(salva.getId());
        assertFalse(pagamentos.isEmpty());
        var codigo = pagamentos.getFirst().getCodigo();

        pagamentoService.confirmarPagamento(codigo);
        assertTrue(pagamentoRepository.existsByReservaIdAndStatus(salva.getId(), StatusPagamento.PAGO));

        var saga = sagaCompraRepository.findByReservaId(salva.getId());
        assertTrue(saga.isPresent());
        assertEquals(SagaEtapa.PAGAMENTO_CONFIRMADO, saga.get().getEtapa());

        reservaService.confirmaVenda(salva);
        assertEquals("V", veiculoRepository.findById(veiculoId).get().getStatus());

        reservaService.retiraVeiculo(salva);
        assertEquals("S", salva.getRetirado());
        assertTrue(documentacaoRetiradaRepository.existsByReservaId(salva.getId()));
    }

    @Test
    void confirmaVenda_semPagamento_deveFalhar() {
        var reserva = criarReservaBasica();
        var salva = reservaService.incluirReserva(reserva);

        assertThrows(Exception.class, () -> reservaService.confirmaVenda(salva));
    }

    @Test
    void cancelamento_liberaVeiculo() {
        var reserva = criarReservaBasica();
        var salva = reservaService.incluirReserva(reserva);

        reservaService.cancela(salva);

        assertEquals("A", veiculoRepository.findById(veiculoId).get().getStatus());
        assertEquals("C", salva.getStatus());
    }

    @Test
    void expiracaoPagamento_liberaVeiculo() {
        var reserva = criarReservaBasica();
        var salva = reservaService.incluirReserva(reserva);

        var pagamento = pagamentoRepository.findByReservaId(salva.getId()).getFirst();
        pagamento.setDataExpiracao(OffsetDateTime.now().minusMinutes(1));
        pagamentoRepository.save(pagamento);

        int expirados = pagamentoService.expirarVencidos();
        assertTrue(expirados >= 1);
        assertEquals("A", veiculoRepository.findById(veiculoId).get().getStatus());
        assertEquals("C", reservaService.obterReservaVendaVeiculoPorId(salva.getId()).get().getStatus());
    }

    private ReservaVendaVeiculo criarReservaBasica() {
        var reserva = new ReservaVendaVeiculo();
        reserva.setValor(new BigDecimal("50000.00"));
        var v = new Veiculo();
        v.setId(veiculoId);
        reserva.setVeiculo(v);
        var c = new Cliente();
        c.setId(clienteId);
        reserva.setCliente(c);
        return reserva;
    }

    private Marca createMarca() {
        var m = new Marca();
        m.setNome("Marca Teste " + System.nanoTime());
        return m;
    }

    private Cor createCor() {
        var cor = new Cor();
        cor.setNome("Preto " + System.nanoTime());
        return cor;
    }
}
