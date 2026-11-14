package net.minecraftforge.fml.common.event;

public class FMLEvent {
    public String getEventType() {
        return getClass().getSimpleName();
    }
}
