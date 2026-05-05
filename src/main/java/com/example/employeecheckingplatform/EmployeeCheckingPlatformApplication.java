package com.example.employeecheckingplatform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
public class EmployeeCheckingPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(EmployeeCheckingPlatformApplication.class, args);
    }

}
