package org.polyfrost.forgecompat.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.block.state.BlockState;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(World.class)
public class WorldMixin {
    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void forgecompat$entityJoin(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (org.polyfrost.forgecompat.Hooks.isSceneWorld((World) (Object) this)) return;
        if (MinecraftForge.EVENT_BUS.post(new EntityJoinWorldEvent(entity, (World) (Object) this)) && !(entity instanceof PlayerEntity)) cir.setReturnValue(false);
    }

    public float getSunBrightnessFactor(float tickDelta) {
        World world = (World) (Object) this;
        float f = 1.0F - (MathHelper.cos(world.getTimeOfDay(tickDelta) * (float) Math.PI * 2.0F) * 2.0F + 0.5F);
        f = MathHelper.clamp(f, 0.0F, 1.0F);
        f = 1.0F - f;
        f = (float) (f * (1.0D - world.getRain(tickDelta) * 5.0F / 16.0D));
        f = (float) (f * (1.0D - world.getThunder(tickDelta) * 5.0F / 16.0D));
        return f;
    }

    public boolean isSideSolid(BlockPos pos, Direction side) {
        return isSideSolid(pos, side, false);
    }

    public boolean isSideSolid(BlockPos pos, Direction side, boolean defaultValue) {
        World world = (World) (Object) this;
        if (!world.isChunkLoaded(pos, false)) return defaultValue;
        return world.getBlockState(pos).getBlock().isCube();
    }

    public void markAndNotifyBlock(BlockPos pos, WorldChunk chunk, BlockState oldState, BlockState newState, int flags) {
        World world = (World) (Object) this;
        if ((flags & 2) != 0 && (!world.isClient || (flags & 4) == 0)) world.notifyBlockChanged(pos);
        if (!world.isClient && (flags & 1) != 0) world.onBlockChanged(pos, oldState.getBlock());
    }
}
