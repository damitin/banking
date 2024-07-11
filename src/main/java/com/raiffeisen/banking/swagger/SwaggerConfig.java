package com.raiffeisen.banking.swagger;

import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                                new Server().url("http://localhost:8080")
                        )
                )
                .info(
                        new Info()
                                .title("Banking API")
                );
    }
}
