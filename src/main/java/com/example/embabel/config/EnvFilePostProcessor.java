package com.example.embabel.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

@Component
public class EnvFilePostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Path envPath = Paths.get(".env");
        if (Files.exists(envPath)) {
            Properties properties = new Properties();
            try (FileInputStream fis = new FileInputStream(envPath.toFile())) {
                properties.load(fis);
                environment.getPropertySources().addFirst(new PropertiesPropertySource("envFile", properties));
            } catch (IOException e) {
                System.err.println("Failed to load .env file: " + e.getMessage());
            }
        }
    }
}
