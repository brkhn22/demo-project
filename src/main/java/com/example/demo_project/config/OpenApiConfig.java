package com.example.demo_project.config;

import java.util.List;

import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            .info(new Info().title("My API").version("1.0"))
            .servers(List.of(new Server().url("http://localhost:8080")))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName, 
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            );

    }

    @Bean
    public OpenApiCustomizer addNgrokHeader() {
        return openApi -> {
            openApi.getPaths().forEach((path, pathItem) -> {
                for (PathItem.HttpMethod method : pathItem.readOperationsMap().keySet()) {
                    Operation operation = pathItem.readOperationsMap().get(method);
                    Parameter ngrokHeader = new HeaderParameter()
                        .name("ngrok-skip-browser-warning")
                        .description("Bypass ngrok browser warning")
                        .required(false)
                        .example("true");
                    operation.addParametersItem(ngrokHeader);
                }
            });
        };
    }
}