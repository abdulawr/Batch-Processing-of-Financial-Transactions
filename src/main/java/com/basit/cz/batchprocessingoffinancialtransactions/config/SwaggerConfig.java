package com.basit.cz.batchprocessingoffinancialtransactions.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Financial Transactions Batch Processing API",
                description = "API documentation for financial transactions batch processing system.",
                version = "1.0"
        )
)
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Financial Transactions Batch Processing API")
                        .description("API documentation for financial transactions batch processing system.")
                        .version("1.0"));
    }
}