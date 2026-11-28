package org.polyfrost.forgecompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.IGuiHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.polyfrost.forgecompat.ForgeCompat;
import org.polyfrost.forgecompat.Hooks;
import org.polyfrost.forgecompat.RavenHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Shadow
    protected void spawnItem(ItemEntity item) {
    }

    @Unique
    private boolean forgecompat$tossing;

    @WrapOperation(method = {"dropItem(Z)Lnet/minecraft/entity/ItemEntity;", "dropItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/ItemEntity;"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/player/PlayerEntity;dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;"))
    private ItemEntity forgecompat$toss(PlayerEntity player, ItemStack stack, boolean dropAround, boolean traceItem, Operation<ItemEntity> original) {
        if (Hooks.isSceneWorld(player.world)) return original.call(player, stack, dropAround, traceItem);
        forgecompat$tossing = true;
        ItemEntity item;
        try {
            item = original.call(player, stack, dropAround, traceItem);
        } finally {
            forgecompat$tossing = false;
        }
        if (item == null) return null;
        ItemTossEvent event = new ItemTossEvent(item, player);
        if (MinecraftForge.EVENT_BUS.post(event)) return null;
        spawnItem(event.entityItem);
        return event.entityItem;
    }

    @WrapWithCondition(method = "dropItem(Lnet/minecraft/item/ItemStack;ZZ)Lnet/minecraft/entity/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/living/player/PlayerEntity;spawnItem(Lnet/minecraft/entity/ItemEntity;)V"))
    private boolean forgecompat$deferTossSpawn(PlayerEntity player, ItemEntity item) {
        return !forgecompat$tossing;
    }

    @Inject(method = "takeDamage", at = @At("HEAD"), cancellable = true)
    private void forgecompat$attacked(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (Hooks.isSceneWorld(player.world)) return;
        if (MinecraftForge.EVENT_BUS.post(new LivingAttackEvent(player, source, amount))) cir.setReturnValue(false);
    }

    @Inject(method = "applyDamage", at = @At("HEAD"), cancellable = true)
    private void forgecompat$hurt(DamageSource source, float amount, CallbackInfo ci, @Local(argsOnly = true) LocalFloatRef amountRef) {
        PlayerEntity entity = (PlayerEntity) (Object) this;
        if (Hooks.isSceneWorld(entity.world) || entity.isInvulnerable(source)) return;
        amountRef.set(ForgeHooks.onLivingHurt(entity, source, amount));
        if (amountRef.get() <= 0) ci.cancel();
    }

    @Inject(method = "copyFrom", at = @At("TAIL"))
    private void forgecompat$clone(PlayerEntity original, boolean keepEverything, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.Clone((PlayerEntity) (Object) this, original, !keepEverything));
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void forgecompat$tickStart(CallbackInfo ci) {
        if (Hooks.isSceneWorld(((PlayerEntity) (Object) this).world)) return;
        MinecraftForge.EVENT_BUS.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.START, (PlayerEntity) (Object) this));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void forgecompat$tickEnd(CallbackInfo ci) {
        if (Hooks.isSceneWorld(((PlayerEntity) (Object) this).world)) return;
        MinecraftForge.EVENT_BUS.post(new TickEvent.PlayerTickEvent(TickEvent.Phase.END, (PlayerEntity) (Object) this));
    }

    public void openGui(Object mod, int id, World world, int x, int y, int z) {
        PlayerEntity player = (PlayerEntity) (Object) this;
        if (!world.isClient) return;
        Object modInstance = mod instanceof String modId ? ForgeCompat.INSTANCES.get(modId) : mod;
        IGuiHandler handler = modInstance == null ? null : NetworkRegistry.guiHandler(modInstance);
        Object gui = handler == null ? null : handler.getClientGuiElement(id, player, world, x, y, z);
        if (gui instanceof Screen screen) Minecraft.getInstance().openScreen(screen);
    }
}
