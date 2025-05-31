package es.upm.etsiinf.tfg.juanmahou.plugin.tables.providers;

import com.google.protobuf.Descriptors.FileDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class FilePackageProvider implements PackageProvider {
    private static final Logger log = LoggerFactory.getLogger(FilePackageProvider.class);
    private final FileDescriptor file;

    public FilePackageProvider(FileDescriptor file) {
        this.file = Objects.requireNonNull(file, "file");
    }

    @Override
    public String getPackage() {
//        log.info("Getting package for file {} (proto package {}) ", file.getName(), file.getPackage());
        if (file.getOptions().hasJavaPackage()) {
//            log.info(" -> javaPackage {}", file.getOptions().getJavaPackage());
            return file.getOptions().getJavaPackage();
        }
        String name = file.getName();
        // Remove .proto extension if present
        if (name.endsWith(".proto")) {
            name = name.substring(0, name.length() - ".proto".length());
        }
        // Convert path separators to dots
        return name.replace('/', '.').replace('\\', '.');
    }
}
