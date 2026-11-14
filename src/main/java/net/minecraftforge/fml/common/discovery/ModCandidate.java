package net.minecraftforge.fml.common.discovery;

import java.io.File;

public class ModCandidate {
    private final File classPathRoot;
    private final File modContainer;
    private final ContainerType sourceType;

    public ModCandidate(File classPathRoot, File modContainer, ContainerType sourceType) {
        this.classPathRoot = classPathRoot;
        this.modContainer = modContainer;
        this.sourceType = sourceType;
    }

    public File getClassPathRoot() {
        return classPathRoot;
    }

    public File getModContainer() {
        return modContainer;
    }

    public ContainerType getSourceType() {
        return sourceType;
    }
}
