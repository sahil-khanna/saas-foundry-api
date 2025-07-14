package com.vonage.saas_foundry_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@EnableRetry
@SpringBootApplication
public class SaasFoundryApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SaasFoundryApiApplication.class, args);
	}

}
