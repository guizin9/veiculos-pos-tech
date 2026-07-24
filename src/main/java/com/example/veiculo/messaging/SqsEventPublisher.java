package com.example.veiculo.messaging;

import com.example.veiculo.geral.config.AwsProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.GetQueueUrlRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Component
@ConditionalOnProperty(name = "app.aws.sqs.enabled", havingValue = "true")
@RequiredArgsConstructor
public class SqsEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(SqsEventPublisher.class);

    private final SqsClient sqsClient;
    private final AwsProperties awsProperties;
    private final ObjectMapper objectMapper;

    private String queueUrl;

    @PostConstruct
    void resolveQueueUrl() {
        if (!awsProperties.getSqs().getQueueUrl().isBlank()) {
            queueUrl = awsProperties.getSqs().getQueueUrl();
            return;
        }
        queueUrl = sqsClient.getQueueUrl(GetQueueUrlRequest.builder()
                .queueName(awsProperties.getSqs().getQueueName())
                .build()).queueUrl();
        awsProperties.getSqs().setQueueUrl(queueUrl);
        log.info("Fila SQS resolvida: {}", queueUrl);
    }

    public void publicar(SagaEvent evento) {
        try {
            String body = objectMapper.writeValueAsString(evento);
            sqsClient.sendMessage(SendMessageRequest.builder()
                    .queueUrl(queueUrl)
                    .messageBody(body)
                    .build());
            log.debug("Evento SAGA publicado: tipo={} reservaId={}", evento.tipo(), evento.reservaId());
        } catch (Exception e) {
            // Falha na fila não deve quebrar a transação principal (consistência eventual)
            log.warn("Falha ao publicar evento SAGA (tipo={}): {}", evento.tipo(), e.getMessage());
        }
    }
}
