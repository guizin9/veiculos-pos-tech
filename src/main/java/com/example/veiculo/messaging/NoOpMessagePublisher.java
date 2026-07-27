package com.example.veiculo.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Fallback quando nenhuma mensageria (SQS/Pub/Sub) está habilitada.
 */
@Component
@ConditionalOnMissingBean({SqsEventPublisher.class, GooglePubSubPublisher.class})
public class NoOpMessagePublisher implements MessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpMessagePublisher.class);

    @Override
    public void publicar(SagaEvent evento) {
        log.trace("Mensageria desabilitada — evento {} não publicado", evento.tipo());
    }
}
