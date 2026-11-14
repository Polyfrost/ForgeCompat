package org.polyfrost.forgecompat.mixin;

import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.ServerPlayerInteractionManager;
import net.minecraft.server.entity.living.player.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {
    @Shadow
    public World world;
    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "startMiningBlock", at = @At("HEAD"), cancellable = true)
    private void forgecompat$leftClickBlock(BlockPos pos, Direction face, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent(player, PlayerInteractEvent.Action.LEFT_CLICK_BLOCK, pos, face, world))) {
            player.networkHandler.sendPacket(new BlockUpdateS2CPacket(world, pos));
            ci.cancel();
        }
    }

    @Inject(method = "useBlock", at = @At("HEAD"), cancellable = true)
    private void forgecompat$rightClickBlock(PlayerEntity player, World world, ItemStack stack, BlockPos pos, Direction face, float dx, float dy, float dz, CallbackInfoReturnable<Boolean> cir) {
        if (MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK, pos, face, world))) {
            ((ServerPlayerEntity) player).networkHandler.sendPacket(new BlockUpdateS2CPacket(world, pos));
            cir.setReturnValue(false);
        }
    }
}
