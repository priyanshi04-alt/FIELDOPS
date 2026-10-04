package com.fieldops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FieldopsApplication {

    public static void main(String[] args) {
        SpringApplication.run(FieldopsApplication.class, args);
    }
}
