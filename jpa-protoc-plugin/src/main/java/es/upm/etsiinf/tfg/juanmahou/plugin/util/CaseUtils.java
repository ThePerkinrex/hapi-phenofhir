package es.upm.etsiinf.tfg.juanmahou.plugin.util;

import java.util.regex.Pattern;

/**
 * Utility for case conversions.
 */
public final class CaseUtils {
    private CaseUtils() {
        // prevent instantiation
    }

    /**
     * Regex pattern to split CamelCase boundaries for snake case conversion.
     */
    private static final Pattern SNAKE_CASE_PATTERN =
            Pattern.compile("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");

    /**
     * Converts a CamelCase or PascalCase name to snake_case.
     *
     * @param name the input string in CamelCase or PascalCase
     * @return the snake_case equivalent, or null if input is null
     */
    public static String toSnakeCase(String name) {
        if (name == null) {
            return null;
        }
        return SNAKE_CASE_PATTERN.matcher(name)
                .replaceAll("_")
                .toLowerCase();
    }

    /**
     * Converts a snake_case, kebab-case, or space-separated string to lowerCamelCase.
     *
     * @param name the input string
     * @return the lowerCamelCase equivalent, or null if input is null
     */
    public static String toLowerCamelCase(String name) {
        if (name == null) {
            return null;
        }
        String[] parts = name.split("[_\\-\\s]+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].toLowerCase();
            if (i == 0) {
                sb.append(part);
            } else if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1));
            }
        }
        return sb.toString();
    }

    /**
     * Converts a snake_case, kebab-case, or space-separated string to UpperCamelCase (PascalCase).
     *
     * @param name the input string
     * @return the UpperCamelCase equivalent, or null if input is null
     */
    public static String toUpperCamelCase(String name) {
        if (name == null) {
            return null;
        }
        String[] parts = name.split("[_\\-\\s]+");
        StringBuilder sb = new StringBuilder();
        for (String rawPart : parts) {
            String part = rawPart.toLowerCase();
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0)))
                        .append(part.substring(1));
            }
        }
        return sb.toString();
    }
}

