package es.upm.etsiinf.tfg.juanmahou.plugin.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;
import java.util.HashMap;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    private Map<String, ConfigTable> messages = new HashMap<>();

    @JsonProperty("base_module")
    private String baseModule;

    public Map<String, ConfigTable> getMessages() {
        return messages;
    }

    @JsonProperty("messages")
    public void setMessages(Map<String, ConfigTable> messages) {
        this.messages = messages;
    }

    public String getBaseModule() {
        return baseModule;
    }

    public void setBaseModule(String baseModule) {
        this.baseModule = baseModule;
    }

    @Override
    public String toString() {
        return "Config{" +
                "messages=" + messages +
                ", baseModule='" + baseModule + '\'' +
                '}';
    }
}

