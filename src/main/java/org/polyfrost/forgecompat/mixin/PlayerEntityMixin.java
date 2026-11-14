package org.polyfrost.forgecompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.polyfrost.forgecompat.RavenHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    private static final float DEFAULT_EYE_HEIGHT = 1.62F;

    public float eyeHeight = getDefaultEyeHeight();

    public String getDisplayNameString() {
        return ((PlayerEntity) (Object) this).getName();
    }

    public float getDefaultEyeHeight() {
        return DEFAULT_EYE_HEIGHT;
    }

    @ModifyExpressionValue(method = "getEyeHeight", at = @At(value = "CONSTANT", args = "floatValue=1.62F"))
    private float forgecompat$eyeHeight(float height) {
        return eyeHeight;
    }

    @Inject(method = "attack(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"), cancellable = true)
    private void forgecompat$attack(Entity target, CallbackInfo ci) {
        if (!ForgeHooks.onPlayerAttackTarget((PlayerEntity) (Object) this, target)) ci.cancel();
    }

    @ModifyExpressionValue(method = "attack(Lnet/minecraft/entity/Entity;)V", at = @At(value = "CONSTANT", args = "doubleValue=0.6D"))
    private double forgecompat$ravenKeepSprint(double slowdown) {
        return RavenHooks.active() ? 1.0D : slowdown;
    }

    @Inject(method = "attack(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/player/PlayerEntity;setSprinting(Z)V"))
    private void forgecompat$ravenKnockback(Entity target, CallbackInfo ci) {
        if (RavenHooks.active()) RavenHooks.knockback(target);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void forgecompat$tickStart(CallbackInfo ci) {
        if (org.polyfrost.forgecompat.Hooks.isSceneWorld(((PlayerEntity) (Object) this).world)) return;
        MinecraftForge.EVENT_BUS.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.START, (PlayerEntity) (Object) this));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void forgecompat$tickEnd(CallbackInfo ci) {
        if (org.polyfrost.forgecompat.Hooks.isSceneWorld(((PlayerEntity) (Object) this).world)) return;
        MinecraftForge.EVENT_BUS.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, (PlayerEntity) (Object) this));
    }

    public void openGui(Object mod, int id, net.minecraft.world.World world, int x, int y, int z) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!world.isClient) return;
        Object modInstance = mod instanceof String modId ? org.polyfrost.forgecompat.ForgeCompat.INSTANCES.get(modId) : mod;
        net.minecraftforge.fml.common.network.IGuiHandler handler = modInstance == null ? null : net.minecraftforge.fml.common.network.NetworkRegistry.guiHandler(modInstance);
        Object gui = handler == null ? null : handler.getClientGuiElement(id, player, world, x, y, z);
        if (gui instanceof net.minecraft.client.gui.screen.Screen screen) net.minecraft.client.Minecraft.getInstance().openScreen(screen);
    }
}
