package es.upm.etsiinf.tfg.juanmahou.mapper.adapter;

@FunctionalInterface
public interface MapperResultAdapter {
    /**
     * @param res the object to adapt
     * @return the adapted object, of the same type
     */
    Object adapt(Object res);
}
