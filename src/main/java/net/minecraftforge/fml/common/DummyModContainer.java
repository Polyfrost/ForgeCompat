package net.minecraftforge.fml.common;

import java.io.File;

public class DummyModContainer implements ModContainer {
    protected ModMetadata md;

    public DummyModContainer() {
        this(new ModMetadata());
    }

    public DummyModContainer(ModMetadata md) {
        this.md = md;
    }

    public DummyModContainer(String label) {
        this(new ModMetadata(label, label, ""));
    }

    @Override
    public String getModId() {
        return md.modId;
    }

    @Override
    public String getName() {
        return md.name;
    }

    @Override
    public String getVersion() {
        return md.version;
    }

    @Override
    public File getSource() {
        return null;
    }

    @Override
    public ModMetadata getMetadata() {
        return md;
    }

    @Override
    public Object getMod() {
        return null;
    }
}
