package net.minecraftforge.client.event.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.sound.SoundCategory;
import net.minecraft.client.sound.SoundPool;
import net.minecraft.client.sound.instance.SoundInstance;
import net.minecraft.client.sound.system.SoundEngine;

public class PlaySoundEvent extends SoundEvent {
    public final String name;
    public final SoundInstance sound;
    public final SoundCategory category;
    public SoundInstance result;

    public PlaySoundEvent(SoundEngine manager, SoundInstance sound) {
        this(manager, sound, categoryOf(sound));
    }

    public PlaySoundEvent(SoundEngine manager, SoundInstance sound, SoundCategory category) {
        super(manager);
        this.sound = sound;
        this.name = sound.getLocation().getPath();
        this.category = category;
        this.result = sound;
    }

    private static SoundCategory categoryOf(SoundInstance sound) {
        SoundPool pool = Minecraft.getInstance().getSoundManager().get(sound.getLocation());
        return pool == null ? null : pool.getCategory();
    }
}
