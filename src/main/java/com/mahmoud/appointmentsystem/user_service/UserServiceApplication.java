package com.mahmoud.appointmentsystem.user_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

	public static void main(String[] args) {
// auto Load environment variables from the.env file based on the active profile

		String profile= System.getProperty("spring.profiles.active", "dev"); // default to "dev" if not set

		Dotenv dotenv = Dotenv.configure()
				.filename(".%s.env".formatted(profile))
				.ignoreIfMissing()
				.load();
		dotenv.entries().forEach(entry ->
				System.setProperty(entry.getKey(), entry.getValue()));

		SpringApplication.run(UserServiceApplication.class, args);
	}

}
