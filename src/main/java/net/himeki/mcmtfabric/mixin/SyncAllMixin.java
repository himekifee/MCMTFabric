package net.himeki.mcmtfabric.mixin;

import net.minecraft.entity.ai.pathing.PathMinHeap;
import net.minecraft.world.tick.ChunkTickScheduler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {PathMinHeap.class, ChunkTickScheduler.class})
public class SyncAllMixin {
}
