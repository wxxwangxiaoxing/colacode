package com.colacode.circle.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI circleOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Colacode Circle API")
                .description("社区圈子服务接口文档")
                .version("1.0")
                .contact(new Contact().name("colacode")));
    }
}
