package net.minecraftforge.fml.common.registry;

import java.util.Iterator;
import java.util.Set;

import net.minecraft.resource.Identifier;
import net.minecraft.util.registry.IdRegistry;

public class FMLControlledNamespacedRegistry<I> extends IdRegistry<Identifier, I> {
    private final IdRegistry<Identifier, I> delegate;

    public FMLControlledNamespacedRegistry(IdRegistry<Identifier, I> delegate) {
        this.delegate = delegate;
    }

    public Identifier getNameForObject(Object thing) {
        return delegate.getKey((I) thing);
    }

    public I getObject(Identifier name) {
        return delegate.get(name);
    }

    public I getObjectById(int id) {
        return delegate.get(id);
    }

    public int getId(Identifier name) {
        I thing = delegate.get(name);
        return thing == null ? -1 : delegate.getId(thing);
    }

    public I getRaw(int id) {
        return delegate.get(id);
    }

    public Iterable<I> typeSafeIterable() {
        return delegate;
    }

    @Override
    public I get(Identifier key) {
        return delegate.get(key);
    }

    @Override
    public Identifier getKey(I value) {
        return delegate.getKey(value);
    }

    @Override
    public boolean containsKey(Identifier key) {
        return delegate.containsKey(key);
    }

    @Override
    public int getId(I value) {
        return delegate.getId(value);
    }

    @Override
    public I get(int id) {
        return delegate.get(id);
    }

    @Override
    public Iterator<I> iterator() {
        return delegate.iterator();
    }

    @Override
    public Set<Identifier> keySet() {
        return delegate.keySet();
    }
}
