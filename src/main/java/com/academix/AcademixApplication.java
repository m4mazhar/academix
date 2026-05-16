package com.academix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AcademixApplication {

    public static void main(String[] args) {
        SpringApplication.run(AcademixApplication.class, args);
    }
}
