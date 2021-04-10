package net.himeki.mcmtfabric.mixin;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import net.himeki.mcmtfabric.parallelised.ConcurrentDoublyLinkedList;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandlerListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity implements ScreenHandlerListener {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Shadow @Final
    private List<Integer> removedEntities = new ConcurrentDoublyLinkedList<Integer>();
}
