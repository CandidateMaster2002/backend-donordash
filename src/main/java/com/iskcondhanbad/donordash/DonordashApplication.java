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
                "MAIL_USERNAME", "MAIL_PASSWORD",
                "WHATSAPP_PHONE_NUMBER_ID", "WHATSAPP_ACCESS_TOKEN",
                "WHATSAPP_TEMPLATE_NAME", "WHATSAPP_TEMPLATE_LANGUAGE",
                "WHATSAPP_API_VERSION",
                "WHATSAPP_PARAM_NAME", "WHATSAPP_PARAM_ADDRESS",
                "WHATSAPP_PARAM_PAN", "WHATSAPP_PARAM_AMOUNT"
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
