package com.ott.api_admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.ott")
@EntityScan(basePackages = "com.ott.domain")
@EnableJpaRepositories(basePackages = "com.ott.domain")
@EnableJpaAuditing
public class ApiAdminApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiAdminApplication.class, args);
	}

}
