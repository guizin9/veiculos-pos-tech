package com.example.veiculo.service;

import com.example.veiculo.geral.config.exception.personal.RegistroNaoEncontradoException;
import com.example.veiculo.model.DocumentacaoRetirada;
import com.example.veiculo.model.ReservaVendaVeiculo;
import com.example.veiculo.model.StatusDocumentacao;
import com.example.veiculo.repository.DocumentacaoRetiradaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentacaoRetiradaService {

    private final DocumentacaoRetiradaRepository documentacaoRetiradaRepository;

    public List<DocumentacaoRetirada> listar() {
        return documentacaoRetiradaRepository.findAll();
    }

    public DocumentacaoRetirada obterPorId(Long id) {
        return documentacaoRetiradaRepository.findById(id)
                .orElseThrow(() -> new RegistroNaoEncontradoException("Documentação com ID " + id + " não encontrada"));
    }

    public Optional<DocumentacaoRetirada> obterPorReserva(Long reservaId) {
        return documentacaoRetiradaRepository.findByReservaId(reservaId);
    }

    /**
     * Emite (simula) a documentação do veículo no momento da retirada.
     * Não gera PDF: cria apenas o registro. Idempotente por reserva.
     */
    @Transactional
    public DocumentacaoRetirada emitir(ReservaVendaVeiculo reserva) {
        var existente = documentacaoRetiradaRepository.findByReservaId(reserva.getId());
        if (existente.isPresent()) return existente.get();

        var agora = OffsetDateTime.now();
        DocumentacaoRetirada doc = new DocumentacaoRetirada();
        doc.setReservaId(reserva.getId());
        doc.setClienteId(reserva.getCliente() != null ? reserva.getCliente().getId() : null);
        doc.setVeiculoId(reserva.getVeiculo() != null ? reserva.getVeiculo().getId() : null);
        doc.setNumeroDocumento(gerarNumeroDocumento());
        doc.setDataEmissao(agora);
        doc.setDataRetirada(agora);
        doc.setStatus(StatusDocumentacao.EMITIDA);
        return documentacaoRetiradaRepository.save(doc);
    }

    private String gerarNumeroDocumento() {
        return "DOC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
