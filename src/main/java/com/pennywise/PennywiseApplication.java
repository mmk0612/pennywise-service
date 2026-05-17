package com.pennywise;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PennywiseApplication {

    public static void main(String[] args) {
        SpringApplication.run(PennywiseApplication.class, args);
    }
}
