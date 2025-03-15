package com.iskcondhanbad.donordash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class DonordashApplication {

	public static void main(String[] args) {
		SpringApplication.run(DonordashApplication.class, args);
	}

}
