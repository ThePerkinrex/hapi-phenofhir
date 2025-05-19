package es.upm.etsiinf.tfg.juanmahou.plugin.tables;


import es.upm.etsiinf.tfg.juanmahou.plugin.util.CaseUtils;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.JavaType;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Table for holding repeated primitive values in a join table style.
 */
public class PrimitiveListTable {
    private final Table parent;
    private final String originName;
    private final JavaType type;

    public PrimitiveListTable(Table parent, String originName, JavaType type) {
        this.parent = Objects.requireNonNull(parent);
        this.originName = Objects.requireNonNull(originName);
        this.type = Objects.requireNonNull(type);
    }

    public String getName() {
        String[] parts = originName.split("_");
        String suffix = Arrays.stream(parts)
                .map(s -> Character.toUpperCase(s.charAt(0)) + s.substring(1))
                .collect(Collectors.joining());
        return parent.getName() + suffix;
    }

    public String getSqlName() {
        return CaseUtils.toSnakeCase(getName());
    }

    public Table getParent() {
        return parent;
    }

    public String getOriginName() {
        return originName;
    }

    public JavaType getType() {
        return type;
    }
}
