package com.example.veiculo.geral.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.aws")
public class AwsProperties {

    /** Habilita integração AWS (SQS, Secrets Manager). */
    private boolean enabled = false;

    /** Endpoint customizado (LocalStack). Vazio = AWS real. */
    private String endpoint = "";

    private String region = "us-east-1";
    private String accessKey = "";
    private String secretKey = "";

    private Sqs sqs = new Sqs();
    private SecretsManager secretsManager = new SecretsManager();

    @Data
    public static class Sqs {
        private boolean enabled = false;
        private String queueName = "veiculos-eventos";
        /** Preenchido automaticamente na inicialização, se vazio. */
        private String queueUrl = "";
        /** Intervalo (ms) do consumer que faz poll na fila. */
        private long pollIntervalMs = 5000;
    }

    @Data
    public static class SecretsManager {
        private boolean enabled = false;
        /** Nome/ARN do segredo com credenciais (JSON: username, password, jwtSecret). */
        private String secretName = "veiculos/app";
    }
}
