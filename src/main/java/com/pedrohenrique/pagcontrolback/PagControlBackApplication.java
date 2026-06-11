package com.pedrohenrique.pagcontrolback;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PagControlBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(PagControlBackApplication.class, args);
    }

}
