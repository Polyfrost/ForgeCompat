package net.minecraftforge.fml.common;

public class MetadataCollection {
    public ModMetadata getMetadataForId(String modId, java.util.Map<String, Object> extraData) {
        return new ModMetadata(modId, modId, "");
    }
}
