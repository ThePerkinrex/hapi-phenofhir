package es.upm.etsiinf.tfg.juanmahou.phenofhir.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class ConfigConfiguration {
    @Bean
    public Config config() throws IOException {
        // TODO use a property
        ObjectMapper objectMapper = new ObjectMapper();
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("config.json")) {
            return objectMapper.readValue(stream, Config.class);
        }
    }
}
