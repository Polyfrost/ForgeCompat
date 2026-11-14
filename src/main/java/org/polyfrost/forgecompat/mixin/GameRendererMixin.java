package org.polyfrost.forgecompat.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import org.polyfrost.forgecompat.Hooks;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.client.render.GameRenderer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow
    private Minecraft minecraft;

    @WrapOperation(method = "render(FJ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(IIF)V"))
    private void forgecompat$drawScreen(Screen screen, int mouseX, int mouseY, float partialTicks, Operation<Void> original) {
        if (!MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.DrawScreenEvent.Pre(screen, mouseX, mouseY, partialTicks))) {
            original.call(screen, mouseX, mouseY, partialTicks);
        }
        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.DrawScreenEvent.Post(screen, mouseX, mouseY, partialTicks));
    }

    @Inject(method = "render(IFJ)V", at = @At(value = "INVOKE_STRING", target = "Lnet/minecraft/util/profiler/Profiler;swap(Ljava/lang/String;)V", args = "ldc=hand"))
    private void forgecompat$renderWorldLast(int pass, float tickDelta, long nanoTime, CallbackInfo ci) {
        if (!org.polyfrost.forgecompat.Hooks.isSceneWorld(minecraft.world)) MinecraftForge.EVENT_BUS.post(new RenderWorldLastEvent(minecraft.worldRenderer, tickDelta));
    }

    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void forgecompat$renderHand(float tickDelta, int pass, org.spongepowered.asm.mixin.injection.callback.CallbackInfo ci) {
        if (!org.polyfrost.forgecompat.Hooks.isSceneWorld(minecraft.world) && net.minecraftforge.client.ForgeHooksClient.renderFirstPersonHand(minecraft.worldRenderer, tickDelta, pass)) ci.cancel();
    }

    @ModifyReturnValue(method = "getFov", at = @At("RETURN"))
    private float forgecompat$fovModifier(float original, float tickDelta, boolean useFovSetting) {
        if (minecraft.world == null || Hooks.isSceneWorld(minecraft.world)) return original;
        Entity entity = minecraft.getCamera();
        EntityViewRenderEvent.FOVModifier event = new EntityViewRenderEvent.FOVModifier((GameRenderer) (Object) this, entity, Camera.getBlockInside(minecraft.world, entity, tickDelta), tickDelta, original);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getFOV();
    }

    @Unique
    private float forgecompat$pitch, forgecompat$yaw;
    @Unique
    private boolean forgecompat$orient;

    @WrapOperation(method = "transformCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;rotatef(FFFF)V", ordinal = 8))
    private void forgecompat$pitch(float angle, float x, float y, float z, Operation<Void> original) {
        forgecompat$pitch = angle;
        forgecompat$orient = true;
    }

    @WrapOperation(method = "transformCamera", at = {
            @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;rotatef(FFFF)V", ordinal = 9),
            @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;rotatef(FFFF)V", ordinal = 10)
    })
    private void forgecompat$yaw(float angle, float x, float y, float z, Operation<Void> original) {
        forgecompat$yaw = angle;
    }

    @Inject(method = "transformCamera", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/platform/GlStateManager;translatef(FFF)V", ordinal = 4))
    private void forgecompat$cameraSetup(float tickDelta, CallbackInfo ci) {
        if (!forgecompat$orient) return;
        forgecompat$orient = false;
        float yaw = forgecompat$yaw, pitch = forgecompat$pitch, roll = 0;
        if (minecraft.world != null && !Hooks.isSceneWorld(minecraft.world)) {
            Entity entity = minecraft.getCamera();
            EntityViewRenderEvent.CameraSetup event = new EntityViewRenderEvent.CameraSetup((GameRenderer) (Object) this, entity, Camera.getBlockInside(minecraft.world, entity, tickDelta), tickDelta, yaw, pitch, roll);
            MinecraftForge.EVENT_BUS.post(event);
            yaw = event.yaw;
            pitch = event.pitch;
            roll = event.roll;
        }
        GlStateManager.rotatef(roll, 0, 0, 1);
        GlStateManager.rotatef(pitch, 1, 0, 0);
        GlStateManager.rotatef(yaw, 0, 1, 0);
    }
}
