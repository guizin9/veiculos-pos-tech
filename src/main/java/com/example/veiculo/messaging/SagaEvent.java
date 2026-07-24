package com.example.veiculo.messaging;

import java.time.OffsetDateTime;
import java.util.Map;

/**
 * Evento publicado na fila SQS para integração assíncrona da SAGA.
 * Consumido pela própria aplicação e/ou por Lambda (LocalStack/AWS).
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
}
