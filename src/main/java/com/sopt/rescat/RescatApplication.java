package com.sopt.rescat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class RescatApplication {

    public static void main(String[] args) {
        SpringApplication.run(RescatApplication.class, args);
    }
}

