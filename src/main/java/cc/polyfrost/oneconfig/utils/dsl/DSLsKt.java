package cc.polyfrost.oneconfig.utils.dsl;

import net.minecraft.client.Minecraft;

public final class DSLsKt {
    private DSLsKt() {
    }

    public static Minecraft getMc() {
        return Minecraft.getInstance();
    }
}
