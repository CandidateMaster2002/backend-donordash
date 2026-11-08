package com.iskcondhanbad.donordash;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class DonordashApplication {

    public static void main(String[] args) {
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();

        String[] envVars = {
                "DATABASE_URL", "DATABASE_USERNAME", "DATABASE_PASSWORD",
                "RAZORPAY_KEY_ID", "RAZORPAY_KEY_SECRET",
                "MAIL_USERNAME", "MAIL_PASSWORD"
        };
        for (String envVar : envVars) {
            String value = System.getenv(envVar);
            if (value == null) {
                value = dotenv.get(envVar);
            }
            if (value != null) {
                System.setProperty(envVar, value);
            }
        }

        SpringApplication.run(DonordashApplication.class, args);
    }
}
