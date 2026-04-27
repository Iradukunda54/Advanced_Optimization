package com.blog.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bloggingPlatformOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Blogging Platform API")
                        .description("RESTful and GraphQL APIs for a modern blogging platform")
                        .version("v1.0.0"));
    }
}






