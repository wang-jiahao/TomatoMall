package com.example.tomatomall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TomatoMallApplication {

    public static void main(String[] args) {
        SpringApplication.run(TomatoMallApplication.class, args);
    }

}
