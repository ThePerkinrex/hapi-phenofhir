package es.upm.etsiinf.tfg.juanmahou.plugin.util;

import java.util.regex.Pattern;

/**
 * Utility for case conversions.
 */
public final class CaseUtils {
    private CaseUtils() {
        // prevent instantiation
    }

    private static final Pattern SNAKE_CASE_PATTERN =
            Pattern.compile("(?<=[a-z])(?=[A-Z])|(?<=[A-Z])(?=[A-Z][a-z])");

    public static String toSnakeCase(String name) {
        if (name == null) {
            return null;
        }
        return SNAKE_CASE_PATTERN.matcher(name)
                .replaceAll("_")
                .toLowerCase();
    }

    /**
     * Converts a snake_case, kebab-case, or space-separated string to lowerCamelCase,
     * but also uppercases any letter immediately following a digit.
     */
    public static String toLowerCamelCase(String name) {
        if (name == null) {
            return null;
        }
        String[] parts = name.split("[_\\-\\s]+");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].toLowerCase();
            if (part.isEmpty()) {
                continue;
            }
            if (i == 0) {
                // First segment: keep first character lowercase, but uppercase letters after digits.
                sb.append(transformForLowerCamel(part));
            } else {
                // Subsequent segments: uppercase first letter (or first letter after digits), then preserve digits.
                sb.append(transformForUpperCamel(part));
            }
        }
        return sb.toString();
    }

    /**
     * Converts a snake_case, kebab-case, or space-separated string to UpperCamelCase (PascalCase),
     * uppercasing first letter of the segment and any letter that follows a digit.
     */
    public static String toUpperCamelCase(String name) {
        if (name == null) {
            return null;
        }
        String[] parts = name.split("[_\\-\\s]+");
        StringBuilder sb = new StringBuilder();
        for (String rawPart : parts) {
            String part = rawPart.toLowerCase();
            if (part.isEmpty()) {
                continue;
            }
            sb.append(transformForUpperCamel(part));
        }
        return sb.toString();
    }

    /**
     * Helper: for lowerCamel, keep the very first letter lowercase,
     * but uppercase any letter that comes immediately after a digit.
     *
     * Examples:
     *   "iso8601duration" -> "iso8601Duration"
     *   "2barBaz"         -> "2BarBaz"
     *   "foo"             -> "foo"
     */
    private static String transformForLowerCamel(String segment) {
        StringBuilder out = new StringBuilder();
        boolean lastWasDigit = false;

        for (int i = 0; i < segment.length(); i++) {
            char c = segment.charAt(i);
            if (i == 0) {
                // first char: always lowercase if letter, even if preceded by digits (there are none here).
                out.append(Character.toLowerCase(c));
            } else {
                if (lastWasDigit && Character.isLetter(c)) {
                    // uppercase a letter that follows a digit
                    out.append(Character.toUpperCase(c));
                } else {
                    // otherwise keep it lowercase
                    out.append(Character.toLowerCase(c));
                }
            }
            lastWasDigit = Character.isDigit(c);
        }
        return out.toString();
    }

    /**
     * Helper: for UpperCamel, uppercase the first letter of the segment
     * (or the first letter after any leading digits), and also uppercase any letter
     * that follows a digit anywhere in the segment.
     *
     * Examples:
     *   "iso8601duration" -> "Iso8601Duration"
     *   "123abc_def"      -> "123AbcDef"
     *   "9lives"          -> "9Lives"
     */
    private static String transformForUpperCamel(String segment) {
        StringBuilder out = new StringBuilder();
        boolean lastWasDigit = false;
        boolean firstLetterHandled = false;

        for (int i = 0; i < segment.length(); i++) {
            char c = segment.charAt(i);

            if (!firstLetterHandled) {
                // We haven’t uppercased any letter yet in this segment:
                if (Character.isLetter(c)) {
                    // If it's a letter (even if preceded by digits), uppercase it.
                    out.append(Character.toUpperCase(c));
                    firstLetterHandled = true;
                } else {
                    // If it’s a digit, just append that digit; wait for the first letter.
                    out.append(c);
                }
            } else {
                // After the first letter has been uppercased, look for digit→letter transitions:
                if (lastWasDigit && Character.isLetter(c)) {
                    out.append(Character.toUpperCase(c));
                } else {
                    out.append(c);
                }
            }

            lastWasDigit = Character.isDigit(c);
        }

        return out.toString();
    }
}


