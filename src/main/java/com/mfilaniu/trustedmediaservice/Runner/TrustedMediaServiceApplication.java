package com.mfilaniu.trustedmediaservice.Runner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EnableJpaRepositories(basePackages = "com.mfilaniu.trustedmediaservice.repository")
@SpringBootApplication(scanBasePackages = {"com.mfilaniu.trustedmediaservice"})
@EntityScan(basePackages = "com.mfilaniu.trustedmediaservice.entity")
public class TrustedMediaServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrustedMediaServiceApplication.class, args);
    }

}
