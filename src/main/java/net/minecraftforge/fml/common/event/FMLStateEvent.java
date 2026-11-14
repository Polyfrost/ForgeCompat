package net.minecraftforge.fml.common.event;

import net.minecraftforge.fml.relauncher.Side;

public class FMLStateEvent extends FMLEvent {
    public Side getSide() {
        return Side.CLIENT;
    }
}
