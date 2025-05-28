package es.upm.etsiinf.tfg.juanmahou.mapper.adapter;

public interface MapperResultAdapter {
    /**
     * @param res the object to adapt
     * @return the adapted object, of the same type
     */
    Object adaptOnSet(Object res);

    /**
     * @param res the object to adapt
     * @return the adapted object, of the same type
     */
    Object adaptOnBuilt(Object res);
}
