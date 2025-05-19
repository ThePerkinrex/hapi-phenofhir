package es.upm.etsiinf.tfg.juanmahou.plugin.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.Collection;
import java.util.ArrayList;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigTable {
    @JsonProperty("as_protobuf")
    private boolean asProtobuf = false;

    private boolean insert = false;

    private String name;

    private List<String> primaryKey;

    private Map<String, ConfigField> fields = Collections.emptyMap();

    public boolean isAsProtobuf() {
        return asProtobuf;
    }

    public void setAsProtobuf(boolean asProtobuf) {
        this.asProtobuf = asProtobuf;
    }

    public boolean isInsert() {
        return insert;
    }

    public void setInsert(boolean insert) {
        this.insert = insert;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * Accept either a single String or a Collection for primary_key.
     */
    @JsonSetter("primary_key")
    public void setPrimaryKey(Object primaryKey) {
        if (primaryKey instanceof String) {
            this.primaryKey = new ArrayList<>();
            this.primaryKey.add((String) primaryKey);
        } else if (primaryKey instanceof Collection) {
            this.primaryKey = ((Collection<?>) primaryKey).stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
        } else {
            this.primaryKey = Collections.emptyList();
        }
    }

    /**
     * Returns the primary key as a list of field names (never null).
     */
    public List<String> getPrimaryKey() {
        return primaryKey != null ? primaryKey : Collections.emptyList();
    }

    public Map<String, ConfigField> getFields() {
        return fields;
    }

    @JsonProperty("fields")
    public void setFields(Map<String, ConfigField> fields) {
        this.fields = fields;
    }

    @Override
    public String toString() {
        return "ConfigTable{" +
                "asProtobuf=" + asProtobuf +
                ", insert=" + insert +
                ", name='" + name + '\'' +
                ", primaryKey=" + primaryKey +
                ", fields=" + fields +
                '}';
    }
}
