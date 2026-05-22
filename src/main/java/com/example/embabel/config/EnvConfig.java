package com.example.embabel.config;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Component
public class EnvConfig {

    @PostConstruct
    public void loadEnv() {
        Path envPath = Paths.get(".env");
        if (Files.exists(envPath)) {
            Properties properties = new Properties();
            try (FileInputStream fis = new FileInputStream(envPath.toFile())) {
                properties.load(fis);
                properties.forEach((key, value) -> {
                    if (System.getenv(key.toString()) == null) {
                        System.setProperty(key.toString(), value.toString());
                    }
                });
            } catch (IOException e) {
                System.err.println("Failed to load .env file: " + e.getMessage());
            }
        }
    }
}
