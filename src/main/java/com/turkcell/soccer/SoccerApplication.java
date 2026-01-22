package com.turkcell.soccer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.turkcell.soccer")
public class SoccerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SoccerApplication.class, args);
	}

}
