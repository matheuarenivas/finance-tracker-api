package com.financetracker.api;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "Finance Tracker API",
        version = "0.0.1",
        description = "Finance Tracker REST API"
    )
)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
