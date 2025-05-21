package es.upm.etsiinf.tfg.juanmahou.plugin.render;

public record Pair<A,B>(A a, B b) {
    public A getA() {
        return a;
    }


    public B getB() {
        return b;
    }
}
