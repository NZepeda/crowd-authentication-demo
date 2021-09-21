package com.nestorzepeda.crowdintegrationdemo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.util.Arrays;

@Import(value={CrowdSecurityConfig.class})
@SpringBootApplication
public class CrowdIntegrationDemoApplication {
	public static void main(String[] args) {
		SpringApplication.run(CrowdIntegrationDemoApplication.class, args);
	}
}
