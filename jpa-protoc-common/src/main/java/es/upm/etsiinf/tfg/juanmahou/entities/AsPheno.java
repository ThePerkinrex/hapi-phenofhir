package es.upm.etsiinf.tfg.juanmahou.entities;

import com.google.protobuf.InvalidProtocolBufferException;

public interface AsPheno<T extends com.google.protobuf.MessageOrBuilder> {
    T asPheno() throws InvalidProtocolBufferException;
}
