package es.upm.etsiinf.tfg.juanmahou.plugin.tables.providers;

public final class PrefixedPackageProvider implements PackageProvider {
    private final PackageProvider inner;
    private final String prefix;

    public PrefixedPackageProvider(PackageProvider inner, String prefix) {
        this.inner = inner;
        this.prefix = prefix;
    }

    @Override
    public String getPackage() {
        return prefix + "." + inner.getPackage();
    }
}
