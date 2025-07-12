package org.fiddich.api;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Arrays;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"org.fiddich"})
public class AutoTimetableApplication {


	public static void main(String[] args) {
		SpringApplication.run(AutoTimetableApplication.class, args);
	}

	@Bean
	public CommandLineRunner checkEnv(Environment env) {
		return args -> {
			System.out.println("DB_URL: " + env.getProperty("DB_URL"));
			System.out.println("Active profiles: " + Arrays.toString(env.getActiveProfiles()));
		};
	}

}
