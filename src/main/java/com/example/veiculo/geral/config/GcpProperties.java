package com.example.veiculo.geral.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "app.gcp")
public class GcpProperties {

    /** Habilita integração GCP (Pub/Sub, Secret Manager). */
    private boolean enabled = false;

    private String projectId = "";

    private PubSub pubsub = new PubSub();

    private CloudSql cloudSql = new CloudSql();

    private SecretManager secretManager = new SecretManager();

    @Data
    public static class SecretManager {
        /** Carrega DB_PASSWORD/JWT_SECRET do Google Secret Manager na inicialização. */
        private boolean enabled = false;
        /** Nome do segredo com a senha do banco. */
        private String dbPasswordSecret = "";
        /** Nome do segredo com o JWT HMAC (mín. 32 caracteres). */
        private String jwtSecret = "";
    }

    @Data
    public static class CloudSql {
        /** Formato: projeto:regiao:instancia (ex.: meu-projeto:us-central1:veiculos-db). */
        private String instanceConnectionName = "";
        private String databaseName = "veiculos";
    }

    @Data
    public static class PubSub {
        private boolean enabled = false;
        /** ID do tópico Pub/Sub (ex.: veiculos-eventos). */
        private String topicId = "veiculos-eventos";
    }
}
