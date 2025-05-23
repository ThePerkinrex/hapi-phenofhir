package es.upm.etsiinf.tfg.juanmahou.phenofhir.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

@Configuration
public class ConfigConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ConfigConfiguration.class);
    private final ObjectMapper objectMapper = new ObjectMapper();



    @Bean
    public Config config(Validator validator) throws IOException {
        // TODO use a property
        try (InputStream stream = getClass().getClassLoader().getResourceAsStream("config.json")) {
            Config c = objectMapper.readValue(stream, Config.class);
            var errors = validator.validate(c);
            if(!errors.isEmpty()) {
                for (var error : errors) {
                    log.error("Error validating config: {} {}, was {}", error.getPropertyPath(), error.getMessage(), error.getInvalidValue());

                }
                throw new ConstraintViolationException("Error validating config", errors);
            }
            return c;
        }
    }
}
