package org.polyfrost.forgecompat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import net.minecraft.client.resource.pack.ZippedResourcePack;

public class ModResourcePack extends ZippedResourcePack {
    private static final String META = "pack.mcmeta";

    public ModResourcePack(File file) {
        super(file);
    }

    @Override
    protected InputStream openResource(String name) throws IOException {
        if (name.equals(META) && !super.hasResource(META)) {
            return new ByteArrayInputStream("{\"pack\":{\"description\":\"Forge mod resources\",\"pack_format\":1}}".getBytes(StandardCharsets.UTF_8));
        }
        return super.openResource(name);
    }

    @Override
    public boolean hasResource(String name) {
        return name.equals(META) || super.hasResource(name);
    }
}
