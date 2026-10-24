package net.minecraft.launchwrapper;

import org.polyfrost.forgecompat.ForgeCompat;

public class LaunchClassLoader {
    public void registerTransformer(String transformerClassName) {
        ForgeCompat.addTransformer(transformerClassName);
    }
}
