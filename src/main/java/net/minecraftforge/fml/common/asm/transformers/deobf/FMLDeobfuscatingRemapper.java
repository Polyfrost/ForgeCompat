package net.minecraftforge.fml.common.asm.transformers.deobf;

import net.minecraft.launchwrapper.LaunchClassLoader;
import org.objectweb.asm.commons.Remapper;
import org.polyfrost.forgecompat.Hooks;

import java.io.File;
import java.util.Set;

public class FMLDeobfuscatingRemapper extends Remapper {
    public static final FMLDeobfuscatingRemapper INSTANCE = new FMLDeobfuscatingRemapper();

    private FMLDeobfuscatingRemapper() {
    }

    public void setupLoadOnly(String deobfFileName, boolean loadAll) {
    }

    public void setup(File mcDir, LaunchClassLoader classLoader, String deobfFileName) {
    }

    public void mergeSuperMaps(String name, String superName, String[] interfaces) {
    }

    public String getStaticFieldType(String oldType, String oldName, String newType, String newName) {
        return Hooks.staticFieldType(oldType, oldName, newType, newName);
    }

    @Override
    public String map(String typeName) {
        return typeName == null ? null : Hooks.deobfClass(typeName);
    }

    public boolean isRemappedClass(String className) {
        return !map(className).equals(className);
    }

    public Set<String> getObfedClasses() {
        return Hooks.obfedClasses();
    }

    public String unmap(String typeName) {
        return typeName == null ? null : Hooks.obfClass(typeName);
    }

    @Override
    public String mapMethodName(String owner, String name, String desc) {
        return Hooks.srgMethod(owner, name, desc);
    }

    @Override
    public String mapFieldName(String owner, String name, String desc) {
        return Hooks.srgField(owner, name, desc);
    }
}
