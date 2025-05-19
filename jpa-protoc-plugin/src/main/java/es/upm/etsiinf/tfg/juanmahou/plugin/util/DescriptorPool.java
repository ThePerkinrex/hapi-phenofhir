package es.upm.etsiinf.tfg.juanmahou.plugin.util;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;

import java.util.HashMap;
import java.util.Map;

public class DescriptorPool {
    private final Map<String, Descriptors.Descriptor> descriptors;
    private final Map<String, Descriptors.FileDescriptor> fileDescriptors;

    public DescriptorPool() {
        this.descriptors = new HashMap<>();
        this.fileDescriptors = new HashMap<>();
    }

    public void add(DescriptorProtos.FileDescriptorProto fileDescriptorProto) throws Descriptors.DescriptorValidationException {
        Descriptors.FileDescriptor fd = Descriptors.FileDescriptor.buildFrom(
                fileDescriptorProto,
                fileDescriptorProto
                        .getDependencyList()
                        .stream()
                        .map(fileDescriptors::get)
                        .toArray(Descriptors.FileDescriptor[]::new));
        this.fileDescriptors.put(fileDescriptorProto.getName(), fd);
        for (Descriptors.Descriptor d : fd.getMessageTypes()) {
            this.descriptors.put(d.getFullName(), d);
        }
    }

    public Descriptors.Descriptor get(String fullname) {
        return descriptors.get(fullname);
    }
}
