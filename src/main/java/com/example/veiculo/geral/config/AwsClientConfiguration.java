package com.example.veiculo.geral.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;

@Configuration
@ConditionalOnProperty(name = "app.aws.enabled", havingValue = "true")
public class AwsClientConfiguration {

    private static final Logger log = LoggerFactory.getLogger(AwsClientConfiguration.class);

    @Bean
    public SqsClient sqsClient(AwsProperties props) {
        var builder = SqsClient.builder().region(Region.of(props.getRegion()));
        configureEndpoint(builder, props);
        log.info("SQS client configurado (endpoint={})", props.getEndpoint().isBlank() ? "AWS" : props.getEndpoint());
        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(name = "app.aws.secrets-manager.enabled", havingValue = "true")
    public SecretsManagerClient secretsManagerClient(AwsProperties props) {
        var builder = SecretsManagerClient.builder().region(Region.of(props.getRegion()));
        configureEndpoint(builder, props);
        log.info("Secrets Manager client configurado");
        return builder.build();
    }

    private static void configureEndpoint(
            software.amazon.awssdk.awscore.client.builder.AwsClientBuilder<?, ?> builder,
            AwsProperties props) {
        if (props.getEndpoint() != null && !props.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(props.getEndpoint()));
            if (props.getAccessKey() != null && !props.getAccessKey().isBlank()) {
                builder.credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey())));
            }
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }
    }
}
