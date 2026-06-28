package com.kahanchale.traveladviser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.kahanchale.traveladviser", "com.kahanchale.splitexpense"})
public class TraveladviserApplication {

	public static void main(String[] args) {
		SpringApplication.run(TraveladviserApplication.class, args);
	}

}
