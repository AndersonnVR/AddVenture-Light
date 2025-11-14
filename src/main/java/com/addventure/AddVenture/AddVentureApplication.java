package com.addventure.AddVenture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class AddVentureApplication {

	public static void main(String[] args) { 
		SpringApplication.run(AddVentureApplication.class, args);
	}

}
