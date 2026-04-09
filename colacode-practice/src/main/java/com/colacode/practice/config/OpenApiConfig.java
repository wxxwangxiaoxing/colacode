package com.colacode.practice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI practiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Colacode Practice API")
                .description("刷题练习服务接口文档")
                .version("1.0")
                .contact(new Contact().name("colacode")));
    }
}
