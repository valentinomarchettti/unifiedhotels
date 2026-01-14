package com.challenge.unifiedhotels;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@SpringBootApplication
public class UnifiedhotelsApplication {

	public static void main(String[] args) {
		SpringApplication.run(UnifiedhotelsApplication.class, args);
	}

}
