package com.bookstore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class OnlineBookstoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(OnlineBookstoreApplication.class, args);
	}

}
