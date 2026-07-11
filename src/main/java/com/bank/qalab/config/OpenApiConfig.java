package com.bank.qalab.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Swagger UI config: adds an "Authorize" button so the QA can paste a Bearer token. */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME = "bearer-jwt";

    @Bean
    OpenAPI bankApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bank QA Lab API")
                        .version("1.0.0")
                        .description("""
                                A practice banking API for QA engineers. Log in via POST /api/auth/login,
                                copy the accessToken, click Authorize and paste it to call protected endpoints.
                                """))
                .addSecurityItem(new SecurityRequirement().addList(SCHEME))
                .components(new Components().addSecuritySchemes(SCHEME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
