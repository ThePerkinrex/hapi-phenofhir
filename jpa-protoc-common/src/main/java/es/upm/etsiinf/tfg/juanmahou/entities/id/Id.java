package es.upm.etsiinf.tfg.juanmahou.entities.id;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Base64;
import java.util.Objects;

public abstract class Id {
    private static ObjectMapper getMapper() {
        return new ObjectMapper().configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
    }

    @Override
    public String toString() {
        ObjectMapper objectMapper = getMapper();
        try {
            byte[] json = objectMapper.writeValueAsBytes(this);
            return Base64.getEncoder().encodeToString(json);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T extends Id> T fromBase64Json(String data, Class<T> cls) throws IOException {
        ObjectMapper objectMapper = getMapper();
        return objectMapper.readValue(Base64.getDecoder().decode(data), cls);
    }

    public static <T extends Id> T fromString(String data, Class<T> cls) throws IOException {
        try {
            Method m = cls.getMethod("fromString", String.class);
            if (!Modifier.isStatic(m.getModifiers()) || !m.getReturnType().equals(cls)) throw new Exception();
            return (T) m.invoke(null, data);
        } catch (Exception e) {
            return Id.fromBase64Json(data, cls);
        }
    }
}
