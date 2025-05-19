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
}

