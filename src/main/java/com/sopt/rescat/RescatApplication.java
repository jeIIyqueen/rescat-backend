package com.sopt.rescat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@EnableJpaAuditing
@SpringBootApplication
public class RescatApplication {

    public static void main(String[] args) {
        SpringApplication.run(RescatApplication.class, args);
    }
}

