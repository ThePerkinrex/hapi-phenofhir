package es.upm.etsiinf.tfg.juanmahou.mapper.resolver;

public final class ResolverUtils {
    public static String[] splitFirst(String path) {
        return path.split("\\|", 2);
    }

    public static String[] getPrefixWithDefault(String path, String def) {
        String[] parts = splitFirst(path);
        String[] res = new String[2];
        if (parts.length == 1) {
            res[0] = def;
            res[1] = path;
        }else{
            res = parts;
        }
        return res;
    }
}
