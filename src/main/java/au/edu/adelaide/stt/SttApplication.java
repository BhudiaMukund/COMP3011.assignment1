package au.edu.adelaide.stt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

/**
 * Entry point for the COMP3011 Assignment 1 speech-to-text service.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
public class SttApplication {

    public static void main(String[] args) {
        SpringApplication.run(SttApplication.class, args);
    }
}