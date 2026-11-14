package org.polyfrost.forgecompat.mixin;

import java.util.Map;

import net.minecraft.client.render.texture.TextureAtlas;
import net.minecraft.client.render.texture.TextureAtlasSprite;
import net.minecraft.client.resource.manager.ResourceManager;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TextureAtlas.class)
public class TextureAtlasMixin {
    @Shadow
    @Final
    private Map<String, TextureAtlasSprite> sourcedSprites;

    public TextureAtlasSprite getTextureExtry(String name) {
        return sourcedSprites.get(name);
    }

    @Inject(method = "loadAndStitch", at = @At("HEAD"))
    private void forgecompat$stitchPre(ResourceManager manager, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new TextureStitchEvent.Pre((TextureAtlas) (Object) this));
    }

    @Inject(method = "loadAndStitch", at = @At("TAIL"))
    private void forgecompat$stitchPost(ResourceManager manager, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new TextureStitchEvent.Post((TextureAtlas) (Object) this));
    }
}
