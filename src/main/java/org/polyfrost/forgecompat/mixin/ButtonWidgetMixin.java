package org.polyfrost.forgecompat.mixin;

import net.minecraft.client.gui.widget.ButtonWidget;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ButtonWidget.class)
public abstract class ButtonWidgetMixin {
    public int packedFGColour;
}
