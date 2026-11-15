package net.minecraftforge.fml.common;

import java.util.Map;

public class MetadataCollection {
    public ModMetadata getMetadataForId(String modId, Map<String, Object> extraData) {
        return new ModMetadata(modId, modId, "");
    }
}
