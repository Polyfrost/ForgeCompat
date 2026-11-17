package net.minecraftforge.client.event.sound;

import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.instance.SoundInstance;
import net.minecraft.client.sound.system.SoundEngine;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public class SoundEvent extends Event {
    public final SoundEngine manager;

    public SoundEvent(SoundEngine manager) {
        this.manager = manager;
    }

    public static Sound getResult(SoundResultEvent event) {
        MinecraftForge.EVENT_BUS.post(event);
        return event.result;
    }

    public static class SoundSourceEvent extends SoundEvent {
        public final SoundInstance sound;
        public final String uuid;
        public final String name;

        public SoundSourceEvent(SoundEngine manager, SoundInstance sound, String uuid) {
            super(manager);
            this.name = sound.getLocation().getPath();
            this.sound = sound;
            this.uuid = uuid;
        }
    }
}
