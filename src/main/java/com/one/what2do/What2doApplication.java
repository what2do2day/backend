package com.one.what2do;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class What2doApplication {

	public static void main(String[] args) {
		SpringApplication.run(What2doApplication.class, args);
	}

}
