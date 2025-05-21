package es.upm.etsiinf.tfg.juanmahou.plugin.render;

import es.upm.etsiinf.tfg.juanmahou.plugin.tables.Table;
import es.upm.etsiinf.tfg.juanmahou.plugin.tables.field.Field;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.Annotation;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.java.ClassType;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.java.PrimitiveType;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.TypeRegistry;
import org.stringtemplate.v4.ST;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableRenderer {
    public static String render(Table table) {
        ST template = TableTemplateManager.getTableST();
        var primaryKey = table.getPrimaryKey();
        var fields = table.getFields();
        List<Field> id_fields = primaryKey.stream().map(fields::get).toList();
        List<Field> rest_fields = fields.entrySet().stream().filter(e -> !primaryKey.contains(e.getKey())).map(Map.Entry::getValue).collect(Collectors.toList());
        rest_fields.addFirst(new Field(table, "id", new PrimitiveType("Key"), List.of(new Annotation(TypeRegistry.EMBEDDED_ID_ANNOTATION))));
        ClassType javaType = table.getJavaType();
        template.add("package", javaType.getPackageName());
        template.add("name", javaType.getName());
        template.add("entity", new Annotation(TypeRegistry.ENTITY_ANNOTATION));
        template.add("fields", rest_fields);
        template.add("pk", new PrimaryKey(id_fields));
        template.add("enum_keys", new ArrayList<>(table.getEnums().keySet()));
        template.add("enum_values", table.getEnums().values().stream().map(v -> v.stream().map(e -> new Pair<>(e.getKey(), e.getValue())).toList()).toList());
        return template.render();
    }
}
