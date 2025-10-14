package com.insurance;

import com.insurance.config.DevDatabaseConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(DevDatabaseConfiguration.class)
public class InsuranceManagementApplication {
    public static void main(String[] args) {
        SpringApplication.run(InsuranceManagementApplication.class, args);
    }
}