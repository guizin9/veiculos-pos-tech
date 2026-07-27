package com.example.veiculo.messaging;

import com.example.veiculo.geral.logging.SagaLoggingContext;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Fachada única para publicação de eventos — delega ao {@link MessagePublisher} ativo (Pub/Sub, SQS ou no-op).
 */
@Component
@RequiredArgsConstructor
public class SagaEventBus {

    private static final Logger log = LoggerFactory.getLogger(SagaEventBus.class);

    private final MessagePublisher publisher;

    public void publicar(SagaEvent evento) {
        if (evento.reservaId() != null) {
            SagaLoggingContext.bindReserva(evento.reservaId());
        }

        Map<String, String> observabilidade = new HashMap<>();
        String correlationId = MDC.get(SagaLoggingContext.CORRELATION_ID);
        if (correlationId != null && !correlationId.isBlank()) {
            observabilidade.put("correlationId", correlationId);
        }
        if (evento.reservaId() != null) {
            observabilidade.put("sagaId", String.valueOf(evento.reservaId()));
        }

        SagaEvent enriched = observabilidade.isEmpty() ? evento : evento.withMetadata(observabilidade);

        // Log estruturado via MDC — sem PII (clienteId omitido)
        log.info("Evento SAGA publicado tipo={} reservaId={} veiculoId={}",
                evento.tipo(), evento.reservaId(), evento.veiculoId());

        publisher.publicar(enriched);
    }
}
