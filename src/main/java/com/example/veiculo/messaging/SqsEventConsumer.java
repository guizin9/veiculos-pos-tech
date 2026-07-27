package com.example.veiculo.messaging;

import com.example.veiculo.geral.config.AwsProperties;
import com.example.veiculo.geral.logging.SagaLoggingContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Consumer interno: faz poll na fila SQS e processa eventos da SAGA.
 * Garante idempotência simples (não reprocessa o mesmo messageId).
 * Em produção, Lambda/EventBridge pode assumir parte deste processamento.
 */
@Component
@ConditionalOnProperty(name = "app.aws.sqs.enabled", havingValue = "true")
@RequiredArgsConstructor
public class SqsEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(SqsEventConsumer.class);

    private final SqsClient sqsClient;
    private final AwsProperties awsProperties;
    private final ObjectMapper objectMapper;

    /** messageId já processados (idempotência em memória; em prod usar deduplication da fila). */
    private final Set<String> processados = ConcurrentHashMap.newKeySet();

    @Scheduled(fixedDelayString = "${app.aws.sqs.poll-interval-ms:5000}")
    public void consumir() {
        String queueUrl = awsProperties.getSqs().getQueueUrl();
        if (queueUrl == null || queueUrl.isBlank()) return;

        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(1)
                .build();

        for (Message message : sqsClient.receiveMessage(request).messages()) {
            if (!processados.add(message.messageId())) continue;

            try {
                SagaEvent evento = objectMapper.readValue(message.body(), SagaEvent.class);
                processar(evento);
                sqsClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build());
            } catch (Exception e) {
                processados.remove(message.messageId());
                log.warn("Erro ao processar mensagem SQS: {}", e.getMessage());
            }
        }
    }

    private void processar(SagaEvent evento) {
        if (evento.metadata() != null) {
            String correlationId = evento.metadata().get("correlationId");
            if (correlationId != null) {
                SagaLoggingContext.bindCorrelationId(correlationId);
            }
        }
        if (evento.reservaId() != null) {
            SagaLoggingContext.bindReserva(evento.reservaId());
        }
        log.info("Evento SAGA consumido tipo={} reservaId={} veiculoId={}",
                evento.tipo(), evento.reservaId(), evento.veiculoId());
    }
}
