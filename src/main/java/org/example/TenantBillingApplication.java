package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class TenantBillingApplication {

    public static void main(String[] args) {
        SpringApplication.run(TenantBillingApplication.class, args);
    }
}
