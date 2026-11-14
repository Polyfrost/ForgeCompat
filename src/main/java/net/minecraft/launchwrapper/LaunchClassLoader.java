package net.minecraft.launchwrapper;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.polyfrost.forgecompat.ForgeCompat;

public class LaunchClassLoader extends URLClassLoader {
    private final List<URL> sources = new ArrayList<>();
    private final List<IClassTransformer> transformers = new ArrayList<>();
    private final Map<String, Class<?>> cachedClasses = new ConcurrentHashMap<>();
    private final Set<String> invalidClasses = ConcurrentHashMap.newKeySet();

    public LaunchClassLoader() {
        super(new URL[0], LaunchClassLoader.class.getClassLoader());
    }

    public LaunchClassLoader(URL[] sources) {
        this();
        for (URL url : sources) addURL(url);
    }

    public void registerTransformer(String transformerClassName) {
        ForgeCompat.addTransformer(transformerClassName);
    }

    @Override
    public void addURL(URL url) {
        sources.add(url);
        try {
            FabricLauncherBase.getLauncher().addToClassPath(Path.of(url.toURI()));
        } catch (URISyntaxException | IllegalArgumentException | UnsupportedOperationException e) {
            super.addURL(url);
        }
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        return Class.forName(name, false, getParent());
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        return Class.forName(name, resolve, getParent());
    }

    public byte[] getClassBytes(String name) throws IOException {
        try (InputStream in = getParent().getResourceAsStream(name.replace('.', '/') + ".class")) {
            return in == null ? null : in.readAllBytes();
        }
    }

    @Override
    public URL getResource(String name) {
        return getParent().getResource(name);
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        return getParent().getResourceAsStream(name);
    }

    public List<URL> getSources() {
        return sources;
    }

    public List<IClassTransformer> getTransformers() {
        return transformers;
    }

    public void addClassLoaderExclusion(String toExclude) {
    }

    public void addTransformerExclusion(String toExclude) {
    }

    public void clearNegativeEntries(Set<String> entriesToClear) {
        invalidClasses.removeAll(entriesToClear);
    }
}
