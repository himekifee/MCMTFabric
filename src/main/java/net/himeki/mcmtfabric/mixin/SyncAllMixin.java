package net.himeki.mcmtfabric.mixin;

import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathMinHeap;
import net.minecraft.util.thread.LockHelper;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.light.LevelPropagator;
import net.minecraft.world.tick.ChunkTickScheduler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {PathMinHeap.class, ChunkTickScheduler.class, LevelPropagator.class, EntityNavigation.class, LockHelper.class})
public class SyncAllMixin {
}
