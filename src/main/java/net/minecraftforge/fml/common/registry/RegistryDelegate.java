package net.minecraftforge.fml.common.registry;

import net.minecraft.resource.Identifier;

public interface RegistryDelegate<T> {
    T get();

    String name();

    Identifier getResourceName();

    Class<T> type();
}
