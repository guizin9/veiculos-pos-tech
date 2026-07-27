package com.example.veiculo.messaging;

import com.example.veiculo.geral.config.GcpProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.auth.oauth2.GoogleCredentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

/**
 * Publica eventos da SAGA no Google Cloud Pub/Sub via REST API (sem gRPC).
 * Evita conflitos de protobuf/grpc no fat-jar do Spring Boot no Cloud Run.
 */
@Component
@ConditionalOnProperty(name = "app.gcp.pubsub.enabled", havingValue = "true")
public class GooglePubSubPublisher implements MessagePublisher {

    private static final Logger log = LoggerFactory.getLogger(GooglePubSubPublisher.class);
    private static final String PUBSUB_SCOPE = "https://www.googleapis.com/auth/pubsub";

    private final GcpProperties gcpProperties;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public GooglePubSubPublisher(GcpProperties gcpProperties) {
        this.gcpProperties = gcpProperties;
    }

    @Override
    public void publicar(SagaEvent evento) {
        try {
            String projectId = gcpProperties.getProjectId();
            String topicId = gcpProperties.getPubsub().getTopicId();
            String url = "https://pubsub.googleapis.com/v1/projects/" + projectId + "/topics/" + topicId + ":publish";

            String json = objectMapper.writeValueAsString(evento);
            String data = Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
            String payload = objectMapper.writeValueAsString(Map.of(
                    "messages", new Object[]{
                            Map.of(
                                    "data", data,
                                    "attributes", Map.of("tipo", evento.tipo().name())
                            )
                    }
            ));

            GoogleCredentials credentials = GoogleCredentials.getApplicationDefault()
                    .createScoped(PUBSUB_SCOPE);
            credentials.refreshIfExpired();
            String token = credentials.getAccessToken().getTokenValue();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json; charset=utf-8")
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("Evento SAGA publicado no Pub/Sub tipo={} reservaId={}", evento.tipo(), evento.reservaId());
            } else {
                log.warn("Pub/Sub retornou HTTP {}: {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.warn("Falha ao publicar evento SAGA no Pub/Sub (tipo={}): {}", evento.tipo(), e.getMessage(), e);
        }
    }
}
