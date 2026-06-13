package com.isc.tokenservice.config;

import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI api() {

        return new OpenAPI()

                .info(
                        new Info()
                                .title(
                                        "Universal Banking API")
                                .version("1.0"))

                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("bearerAuth"))

                .components(
                        new Components()
                                .addSecuritySchemes(
                                        "bearerAuth",

                                        new SecurityScheme()
                                                .name(
                                                        "bearerAuth")
                                                .type(
                                                        SecurityScheme.Type.HTTP)
                                                .scheme(
                                                        "bearer")
                                                .bearerFormat(
                                                        "JWT")));
    }
}