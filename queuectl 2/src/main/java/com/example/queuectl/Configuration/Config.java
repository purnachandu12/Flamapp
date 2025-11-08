package com.example.queuectl.Configuration;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

@Component
public class Config {

    private final Environment env;

    public Config(Environment env) {
        this.env = env;
    }

    @Getter
    @Value("${queuectl.max-retries:3}")
    private int maxRetries;

    @Getter
    @Value("${queuectl.backoff-base:2}")
    private int backoffBase;

    public void setProperty(String key, String value) {
        try (OutputStream output = new FileOutputStream("src/main/resources/application.properties", true)) {
            Properties props = new Properties();
            props.setProperty(key, value);
            props.store(output, "Updated by QueueCTL CLI");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return env.getProperty(key);
    }
}
