package org.polyfrost.forgecompat.mixin;

import java.nio.file.Path;
import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.ClientPlayerInteractionManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.TickTimer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.pack.ResourcePack;
import net.minecraft.client.resource.pack.ZippedResourcePack;
import net.minecraft.client.entity.living.player.LocalClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraft.client.render.GameRenderer;
import org.polyfrost.forgecompat.ForgeCompat;
import org.polyfrost.forgecompat.RavenHooks;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Shadow
    private TickTimer timer;

    @Shadow
    public ClientWorld world;

    @Shadow
    @Final
    private List<ResourcePack> requiredResourcePacks;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;reloadResources()V", ordinal = 0))
    private void forgecompat$modResources(CallbackInfo ci) {
        for (Path jar : ForgeCompat.RESOURCE_JARS) requiredResourcePacks.add(new org.polyfrost.forgecompat.ModResourcePack(jar.toFile()));
    }

    @WrapMethod(method = "openScreen")
    private void forgecompat$guiOpen(Screen screen, Operation<Void> original) {
        GuiOpenEvent event = new GuiOpenEvent(screen);
        if (!MinecraftForge.EVENT_BUS.post(event)) original.call(event.gui);
    }

    @Inject(method = "setWorld(Lnet/minecraft/client/world/ClientWorld;Ljava/lang/String;)V", at = @At("HEAD"))
    private void forgecompat$worldUnload(ClientWorld world, String message, CallbackInfo ci) {
        ClientWorld previous = org.polyfrost.forgecompat.Hooks.realWorld();
        if (previous != null && previous != world) MinecraftForge.EVENT_BUS.post(new WorldEvent.Unload(previous));
    }

    @Inject(method = "setWorld(Lnet/minecraft/client/world/ClientWorld;Ljava/lang/String;)V", at = @At("TAIL"))
    private void forgecompat$worldLoad(ClientWorld world, String message, CallbackInfo ci) {
        ClientWorld previous = org.polyfrost.forgecompat.Hooks.realWorld();
        org.polyfrost.forgecompat.Hooks.setRealWorld(world);
        if (world != null && world != previous) MinecraftForge.EVENT_BUS.post(new WorldEvent.Load(world));
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void forgecompat$tickStart(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.START));
    }

    @Inject(method = "tick()V", at = @At("TAIL"))
    private void forgecompat$tickEnd(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new TickEvent.ClientTickEvent(TickEvent.Phase.END));
    }

    @Inject(method = "runGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;render(FJ)V"))
    private void forgecompat$renderTickStart(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new TickEvent.RenderTickEvent(TickEvent.Phase.START, timer.partialTick));
    }

    @Inject(method = "runGame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;render(FJ)V", shift = At.Shift.AFTER))
    private void forgecompat$renderTickEnd(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new TickEvent.RenderTickEvent(TickEvent.Phase.END, timer.partialTick));
    }

    @Unique
    private boolean forgecompat$mouseInput;

    @Unique
    private boolean forgecompat$keyInput;

    @WrapOperation(method = "tick()V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Mouse;next()Z", remap = false))
    private boolean forgecompat$mouseEvent(Operation<Boolean> original) {
        if (forgecompat$mouseInput) MinecraftForge.EVENT_BUS.post(new InputEvent.MouseInputEvent());
        while (original.call()) {
            if (!MinecraftForge.EVENT_BUS.post(new MouseEvent())) return forgecompat$mouseInput = true;
        }
        return forgecompat$mouseInput = false;
    }

    @WrapOperation(method = "tick()V", at = @At(value = "INVOKE", target = "Lorg/lwjgl/input/Keyboard;next()Z", remap = false))
    private boolean forgecompat$keyEvent(Operation<Boolean> original) {
        if (forgecompat$keyInput) MinecraftForge.EVENT_BUS.post(new InputEvent.KeyInputEvent());
        return forgecompat$keyInput = original.call();
    }

    @WrapOperation(method = "tick()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;pick(F)V"))
    private void forgecompat$ravenPick(GameRenderer renderer, float tickDelta, Operation<Void> original) {
        if (RavenHooks.active()) RavenHooks.pick();
        else original.call(renderer, tickDelta);
    }

    @WrapOperation(method = "doUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/ClientPlayerInteractionManager;useBlock(Lnet/minecraft/client/entity/living/player/LocalClientPlayerEntity;Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/Direction;Lnet/minecraft/util/math/Vec3d;)Z"))
    private boolean forgecompat$rightClickBlock(ClientPlayerInteractionManager manager, LocalClientPlayerEntity player, ClientWorld world, ItemStack stack, BlockPos pos, Direction face, Vec3d hit, Operation<Boolean> original) {
        return !MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, pos, face, world)) && original.call(manager, player, world, stack, pos, face, hit);
    }
}
