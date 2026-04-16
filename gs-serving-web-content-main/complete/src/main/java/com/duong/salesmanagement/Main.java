package com.duong.salesmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void printAppUrl() {
        String url = "http://localhost:8080";
        System.out.println();
        System.out.println("=======================================");
        System.out.println(" RUN AT:");
        System.out.println(" " + url);
        System.out.println("=======================================");
        System.out.println();
    }
}
