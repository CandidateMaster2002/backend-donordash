package com.iskcondhanbad.donordash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
public class DonordashApplication {

	public static void main(String[] args) {
		// Access environment variables directly from the system environment
		System.out.println("DATABASE_URL: " + System.getenv("DATABASE_URL"));
		System.out.println("RAZORPAY_KEY_ID: " + System.getenv("RAZORPAY_KEY_ID"));
        
		// Set system properties if you want to pass them to Spring Boot application
		System.setProperty("DATABASE_URL", System.getenv("DATABASE_URL"));
        System.setProperty("DATABASE_USERNAME", System.getenv("DATABASE_USERNAME"));
        System.setProperty("DATABASE_PASSWORD", System.getenv("DATABASE_PASSWORD"));
        System.setProperty("RAZORPAY_KEY_ID", System.getenv("RAZORPAY_KEY_ID"));
        System.setProperty("RAZORPAY_KEY_SECRET", System.getenv("RAZORPAY_KEY_SECRET"));
        System.setProperty("MAIL_USERNAME", System.getenv("MAIL_USERNAME"));
        System.setProperty("MAIL_PASSWORD", System.getenv("MAIL_PASSWORD"));

		SpringApplication.run(DonordashApplication.class, args);
	}
}
