package ru.isands.test.estore;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(servers = {@Server(url = "/", description = "EStore Server")})
public class EStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(EStoreApplication.class, args);
    }
}
