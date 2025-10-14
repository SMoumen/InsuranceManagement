package com.insurance.config;

import org.springframework.boot.devtools.restart.RestartScope;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration(proxyBeanMethods = false)
@SuppressWarnings("SpringBootTestcontainersInRuntime")
public class DevDatabaseConfiguration {

    @Bean
    @ServiceConnection
    @Profile("dev")
    @RestartScope
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"))
                .withDatabaseName("insurance_dev")
                .withUsername("dev_user")
                .withPassword("dev_password")
                .withInitScript("schema.sql")
                .withReuse(true);
    }
}