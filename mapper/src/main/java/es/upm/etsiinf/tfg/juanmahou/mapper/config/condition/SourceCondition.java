package es.upm.etsiinf.tfg.juanmahou.mapper.config.condition;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(ProfileCondition.class)
})
public abstract class SourceCondition {
}
