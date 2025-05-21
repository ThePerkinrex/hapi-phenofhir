package es.upm.etsiinf.tfg.juanmahou.hibernate.generators;

public class GeneratorNotFoundException extends Exception {
    public GeneratorNotFoundException(String name) {
        super("Generator not found: " + name);
    }
}
