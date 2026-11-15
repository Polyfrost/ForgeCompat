package net.minecraftforge.client.event.sound;

import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.system.SoundEngine;

public abstract class SoundResultEvent extends SoundEvent {
    public final SoundEngine manager;
    public final Sound source;
    public final String name;
    public final float volume;
    public final float pitch;
    public Sound result;

    public SoundResultEvent(SoundEngine manager, Sound source, String name, float volume, float pitch) {
        super(manager);
        this.manager = manager;
        this.source = source;
        this.name = name;
        this.volume = volume;
        this.pitch = pitch;
        this.result = source;
    }
}
