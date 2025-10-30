package com.allocat.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.allocat"})
public class AllocatApplication {

    public static void main(String[] args) {
        SpringApplication.run(AllocatApplication.class, args);
    }
}


