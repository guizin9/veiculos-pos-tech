package com.example.veiculo.geral.config;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Carrega {@code DB_PASSWORD} e {@code JWT_SECRET} do Google Secret Manager quando habilitado.
 * Variáveis de ambiente explícitas (ou montagem nativa do Cloud Run via {@code --set-secrets}) têm prioridade.
 */
public class GcpSecretManagerEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(GcpSecretManagerEnvironmentPostProcessor.class);
    private static final String PROPERTY_SOURCE = "gcpSecretManager";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        if (!Boolean.parseBoolean(environment.getProperty("app.gcp.secret-manager.enabled", "false"))) {
            return;
        }

        String projectId = environment.getProperty("app.gcp.project-id");
        if (projectId == null || projectId.isBlank()) {
            throw new IllegalStateException(
                    "app.gcp.project-id é obrigatório quando app.gcp.secret-manager.enabled=true");
        }

        Map<String, Object> secrets = new HashMap<>();

        if (isBlank(environment.getProperty("DB_PASSWORD"))) {
            String secretName = environment.getProperty("app.gcp.secret-manager.db-password-secret");
            if (!isBlank(secretName)) {
                secrets.put("DB_PASSWORD", fetchSecret(projectId, secretName));
                log.info("DB_PASSWORD carregado do Secret Manager (secret={})", secretName);
            }
        }

        if (isBlank(environment.getProperty("JWT_SECRET"))) {
            String secretName = environment.getProperty("app.gcp.secret-manager.jwt-secret");
            if (!isBlank(secretName)) {
                secrets.put("JWT_SECRET", fetchSecret(projectId, secretName));
                log.info("JWT_SECRET carregado do Secret Manager (secret={})", secretName);
            }
        }

        if (!secrets.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE, secrets));
        }
    }

    static String fetchSecret(String projectId, String secretId) {
        SecretVersionName versionName = SecretVersionName.of(projectId, secretId, "latest");
        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
            return client.accessSecretVersion(versionName)
                    .getPayload()
                    .getData()
                    .toStringUtf8()
                    .trim();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Falha ao acessar segredo '%s' no projeto '%s': %s".formatted(secretId, projectId, e.getMessage()), e);
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
