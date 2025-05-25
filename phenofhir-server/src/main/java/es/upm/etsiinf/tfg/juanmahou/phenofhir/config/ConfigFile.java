package es.upm.etsiinf.tfg.juanmahou.phenofhir.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.InputStream;

@Component
public class ConfigFile {
    @Value("${phenofhir.config.file:config.json}")
    private String configFile;

    public InputStream loadConfig() {
        return getClass().getClassLoader().getResourceAsStream(configFile);
    }
}
