package com.zalopay.zalowallet;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ZalowalletServiceApplication {

    public static void main(String[] args) {
        String x = "${server.port}";
        SpringApplication.run(ZalowalletServiceApplication.class, args);
    }

}
