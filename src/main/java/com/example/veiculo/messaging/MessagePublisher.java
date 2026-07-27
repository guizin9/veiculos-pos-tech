package com.example.veiculo.messaging;

/**
 * Contrato de publicação de eventos da SAGA — implementações: SQS (AWS), Pub/Sub (GCP) ou no-op.
 */
public interface MessagePublisher {

    void publicar(SagaEvent evento);
}
