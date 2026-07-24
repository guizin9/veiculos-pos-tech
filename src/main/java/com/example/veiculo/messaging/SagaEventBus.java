package com.example.veiculo.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Fachada única para publicação de eventos — usa SQS quando habilitado, senão no-op.
 */
@Component
@RequiredArgsConstructor
public class SagaEventBus {

    private final java.util.Optional<SqsEventPublisher> sqsPublisher;
    private final NoOpSagaEventPublisher noOpPublisher;

    public void publicar(SagaEvent evento) {
        sqsPublisher.ifPresentOrElse(p -> p.publicar(evento), () -> noOpPublisher.publicar(evento));
    }
}
