package net.minecraftforge.client;

import net.minecraft.block.BedBlock;
import net.minecraft.block.state.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.model.block.BakedModel;
import net.minecraft.client.render.platform.GlStateManager;
import net.minecraft.client.render.world.WorldRenderer;
import net.minecraft.client.resource.model.ModelTransformations;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;

public class ForgeHooksClient {
    public static BakedModel handleCameraTransforms(BakedModel model, ModelTransformations.Type type) {
        model.getTransformations().apply(type);
        return model;
    }

    public static void orientBedCamera(WorldView world, BlockPos pos, BlockState state, Entity entity) {
        if (state.getBlock() instanceof BedBlock) {
            GlStateManager.rotatef(state.get(BedBlock.FACING).getIdHorizontal() * 90, 0.0F, 1.0F, 0.0F);
        }
    }

    public static void renderTileItem(Item item, int metadata) {
    }

    public static void drawScreen(Screen screen, int mouseX, int mouseY, float partialTicks) {
        screen.render(mouseX, mouseY, partialTicks);
    }

    public static boolean renderFirstPersonHand(WorldRenderer context, float partialTicks, int renderPass) {
        return MinecraftForge.EVENT_BUS.hasListeners(RenderHandEvent.class)
                && MinecraftForge.EVENT_BUS.post(new RenderHandEvent(context, partialTicks, renderPass));
    }

    public static void dispatchRenderLast(WorldRenderer context, float partialTicks) {
        if (MinecraftForge.EVENT_BUS.hasListeners(RenderWorldLastEvent.class))
            MinecraftForge.EVENT_BUS.post(new RenderWorldLastEvent(context, partialTicks));
    }
}
