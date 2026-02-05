package com.example.metricsdemo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userManagementAPI() {
        Server localServer = new Server();
        localServer.setUrl("http://localhost:8080");
        localServer.setDescription("Local Development Server");

        Contact contact = new Contact();
        contact.setName("User Management API");
        contact.setEmail("support@example.com");

        Info info = new Info()
                .title("User Management API")
                .version("1.0.0")
                .description("REST API for User Management with Redis Caching, Prometheus Metrics, and Loki Logging")
                .contact(contact);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer));
    }
}
