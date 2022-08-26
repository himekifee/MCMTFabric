package net.himeki.mcmtfabric.mixin;

import net.minecraft.entity.ai.pathing.PathMinHeap;
import net.minecraft.world.chunk.light.LevelPropagator;
import net.minecraft.world.tick.ChunkTickScheduler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {PathMinHeap.class, ChunkTickScheduler.class, LevelPropagator.class})
public class SyncAllMixin {
}
