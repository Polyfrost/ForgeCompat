package org.polyfrost.forgecompat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.living.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.ServerPlayerInteractionManager;
import net.minecraft.server.network.handler.ServerPlayNetworkHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @WrapOperation(method = "handlePlayerUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/ServerPlayerInteractionManager;useItem(Lnet/minecraft/entity/living/player/PlayerEntity;Lnet/minecraft/world/World;Lnet/minecraft/item/ItemStack;)Z"))
    private boolean forgecompat$rightClickAir(ServerPlayerInteractionManager manager, PlayerEntity player, World world, ItemStack stack, Operation<Boolean> original) {
        if (MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, new BlockPos(0, 0, 0), null, world))) return false;
        return original.call(manager, player, world, stack);
    }
}
