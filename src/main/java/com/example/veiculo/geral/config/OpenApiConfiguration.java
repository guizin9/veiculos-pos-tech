package com.example.veiculo.geral.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI openAPI() {
        final String bearer = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("API de Revenda de Veículos")
                        .description("API REST — FASE 5 FIAP Pós Tech. Autentique via POST /auth/login e use Bearer token.")
                        .version("0.0.1-SNAPSHOT"))
                .addSecurityItem(new SecurityRequirement().addList(bearer))
                .components(new Components().addSecuritySchemes(bearer,
                        new SecurityScheme()
                                .name(bearer)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
