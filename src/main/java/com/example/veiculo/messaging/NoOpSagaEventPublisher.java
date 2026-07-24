package com.example.veiculo.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Fallback quando SQS está desabilitado (dev local sem Docker/LocalStack).
 */
@Component
@ConditionalOnMissingBean(SqsEventPublisher.class)
public class NoOpSagaEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(NoOpSagaEventPublisher.class);

    public void publicar(SagaEvent evento) {
        log.trace("SQS desabilitado — evento {} não publicado", evento.tipo());
    }
}
