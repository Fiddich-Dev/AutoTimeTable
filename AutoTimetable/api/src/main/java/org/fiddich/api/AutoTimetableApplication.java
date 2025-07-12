package org.fiddich.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(scanBasePackages = {"org.fiddich"})
public class AutoTimetableApplication {


	public static void main(String[] args) {
		SpringApplication.run(AutoTimetableApplication.class, args);
	}

}
