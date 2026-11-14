package org.polyfrost.forgecompat.mixin;

import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin {
    @Shadow
    protected List<ButtonWidget> buttons;

    @Shadow
    protected Minecraft minecraft;

    @WrapOperation(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;init()V"))
    private void forgecompat$initGuiPre(Screen screen, Operation<Void> original) {
        if (!MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Pre(screen, buttons))) original.call(screen);
    }

    @Inject(method = "init(Lnet/minecraft/client/Minecraft;II)V", at = @At("TAIL"))
    private void forgecompat$initGuiPost(Minecraft minecraft, int width, int height, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.InitGuiEvent.Post((Screen) (Object) this, buttons));
    }

    @WrapOperation(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;mouseClicked(Lnet/minecraft/client/Minecraft;II)Z"))
    private boolean forgecompat$actionPerformedPre(ButtonWidget button, Minecraft minecraft, int mouseX, int mouseY, Operation<Boolean> original) {
        return original.call(button, minecraft, mouseX, mouseY) && !MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.ActionPerformedEvent.Pre((Screen) (Object) this, button, buttons));
    }

    @WrapOperation(method = "mouseClicked", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;buttonClicked(Lnet/minecraft/client/gui/widget/ButtonWidget;)V"))
    private void forgecompat$actionPerformedPost(Screen screen, ButtonWidget button, Operation<Void> original) {
        original.call(screen, button);
        if (equals(minecraft.screen)) MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.ActionPerformedEvent.Post(screen, button, buttons));
    }

    @Inject(method = "sendChatMessage(Ljava/lang/String;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/living/player/LocalClientPlayerEntity;sendChat(Ljava/lang/String;)V"), cancellable = true)
    private void forgecompat$clientCommand(String message, boolean addToHistory, CallbackInfo ci) {
        if (ClientCommandHandler.instance.run(minecraft.player, message) != 0) ci.cancel();
    }

    @WrapOperation(method = "handleInputs", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;handleKeyboard()V"))
    private void forgecompat$keyboardInput(Screen screen, Operation<Void> original) {
        if (MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.KeyboardInputEvent.Pre(screen))) return;
        original.call(screen);
        if (screen.equals(minecraft.screen)) MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.KeyboardInputEvent.Post(screen));
    }

    @Inject(method = "renderBackground(I)V", at = @At("TAIL"))
    private void forgecompat$backgroundDrawn(int offset, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.hasListeners(GuiScreenEvent.BackgroundDrawnEvent.class)) MinecraftForge.EVENT_BUS.post(new GuiScreenEvent.BackgroundDrawnEvent((Screen) (Object) this));
    }
}
