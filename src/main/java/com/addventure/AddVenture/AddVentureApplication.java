package com.addventure.AddVenture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import io.github.cdimascio.dotenv.Dotenv;

@EnableScheduling
@SpringBootApplication
public class AddVentureApplication {

	public static void main(String[] args) {

		// Cargar solo la contraseña desde el archivo .env
		Dotenv dotenv = Dotenv.configure()
				.filename(".env")
				.directory("src/main/resources")
				.load();

		System.setProperty("BD_PASSWORD", dotenv.get("BD_PASSWORD"));

		SpringApplication.run(AddVentureApplication.class, args);
	}

}
