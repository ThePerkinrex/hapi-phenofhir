package es.upm.etsiinf.tfg.juanmahou.plugin.types.source;

import java.util.Objects;

public class RepeatedSourceType implements SourceType{
    private final SourceType sourceType;

    public RepeatedSourceType(SourceType sourceType) {
        this.sourceType = sourceType;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    @Override
    public String toString() {
        return "RepeatedSourceType{" +
                "sourceType=" + sourceType +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RepeatedSourceType that = (RepeatedSourceType) o;
        return Objects.equals(sourceType, that.sourceType);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(sourceType);
    }
}
