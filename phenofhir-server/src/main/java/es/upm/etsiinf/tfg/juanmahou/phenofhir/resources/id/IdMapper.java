package es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.id;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import es.upm.etsiinf.tfg.juanmahou.entities.id.Id;
import es.upm.etsiinf.tfg.juanmahou.entities.id.WithId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class IdMapper {
    private static final Logger log = LoggerFactory.getLogger(IdMapper.class);
    private final Charset charset;
    private final ObjectMapper mapper;
    private final Base64.Encoder base64Encoder;
    private final Base64.Decoder base64Decoder;

    public IdMapper() {
        mapper = new ObjectMapper()
                .configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
//                .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        base64Encoder = Base64.getEncoder();
        base64Decoder = Base64.getDecoder();
        charset = StandardCharsets.UTF_8;
    }

    public String getId(WithId<? extends Id> pheno) throws JsonProcessingException {
        String json = mapper.writeValueAsString(pheno.getId());
        return new String(base64Encoder.encode(json.getBytes(charset)), charset);
    }


    private static <K extends Id, T extends WithId<K>> Class<K> getIdClass(Class<T> cls) throws NoSuchMethodException {
        return (Class<K>) cls.getMethod("getId").getReturnType();
    }

    public <K extends Id, W extends WithId<K>> K getId(Class<W> target, String id) throws InvalidIdException, ClassNotFoundException, JsonProcessingException {
        log.info("Loading key for target {}: {}", target, id);
        Class<K> key;
        try {
            key = getIdClass(target);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unexpected error", e);
        }
        String json = new String(base64Decoder.decode(id.getBytes(charset)), charset);
        log.info("Key class: {}; JSON: {}", key, json);
        return mapper.readValue(json, key);
    }
}
