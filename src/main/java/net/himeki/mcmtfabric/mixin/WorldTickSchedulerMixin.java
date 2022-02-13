package net.himeki.mcmtfabric.mixin;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.himeki.mcmtfabric.parallelised.ConcurrentCollections;
import net.himeki.mcmtfabric.parallelised.fastutil.Long2ObjectOpenConcurrentHashMap;
import net.minecraft.world.tick.ChunkTickScheduler;
import net.minecraft.world.tick.OrderedTick;
import net.minecraft.world.tick.QueryableTickScheduler;
import net.minecraft.world.tick.WorldTickScheduler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.CopyOnWriteArrayList;

@Mixin(WorldTickScheduler.class)
public abstract class WorldTickSchedulerMixin<T> implements QueryableTickScheduler<T> {
    @Shadow
    @Final
    private final Long2ObjectMap<ChunkTickScheduler<T>> chunkTickSchedulers = new Long2ObjectOpenConcurrentHashMap<>();

//    @Shadow
//    @Final
//    private final Long2LongMap nextTriggerTickByChunkPos = new Long2LongConcurrentHashMap(9223372036854775807L);

    @Shadow
    @Final
    private final Queue<ChunkTickScheduler<T>> tickableChunkTickSchedulers = ConcurrentCollections.newArrayDeque();

    @Shadow
    @Final
    private final Queue<OrderedTick<T>> tickableTicks = ConcurrentCollections.newArrayDeque();

    @Shadow
    @Final
    private final List<OrderedTick<T>> tickedTicks = new CopyOnWriteArrayList<>();
}
