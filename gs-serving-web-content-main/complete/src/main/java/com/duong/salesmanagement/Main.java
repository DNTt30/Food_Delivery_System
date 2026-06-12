package com.duong.salesmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.awt.Desktop;
import java.net.URI;

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

        // Tự động mở browser tại trang chủ
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception e) { 
            System.out.println("Không thể tự động mở browser: " + e.getMessage());
        }
    }
}
