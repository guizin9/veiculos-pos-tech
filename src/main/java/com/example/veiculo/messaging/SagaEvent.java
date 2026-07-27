package com.example.veiculo.messaging;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Evento publicado na mensageria (SQS ou Pub/Sub) para integração assíncrona da SAGA.
 * Consumido pela própria aplicação e/ou por Lambda/Cloud Function.
 */
public record SagaEvent(
        SagaEventType tipo,
        Long reservaId,
        Long clienteId,
        Long veiculoId,
        String codigoPagamento,
        OffsetDateTime timestamp,
        Map<String, String> metadata
) {
    public static SagaEvent of(SagaEventType tipo, Long reservaId, Long clienteId, Long veiculoId) {
        return new SagaEvent(tipo, reservaId, clienteId, veiculoId, null, OffsetDateTime.now(), Map.of());
    }

    public static SagaEvent of(SagaEventType tipo, Long reservaId, Long clienteId, Long veiculoId, String codigoPagamento) {
        return new SagaEvent(tipo, reservaId, clienteId, veiculoId, codigoPagamento, OffsetDateTime.now(), Map.of());
    }

    /** Mescla metadados de observabilidade (correlationId, sagaId) sem alterar payload de negócio. */
    public SagaEvent withMetadata(Map<String, String> extra) {
        Map<String, String> merged = new HashMap<>(metadata);
        merged.putAll(extra);
        return new SagaEvent(tipo, reservaId, clienteId, veiculoId, codigoPagamento, timestamp, Map.copyOf(merged));
    }
}
