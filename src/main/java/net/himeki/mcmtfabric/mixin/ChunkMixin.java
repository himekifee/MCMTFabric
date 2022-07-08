package net.himeki.mcmtfabric.mixin;

import net.himeki.mcmtfabric.parallelised.ConcurrentCollections;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(Chunk.class)
public abstract class ChunkMixin {

    @Shadow
    @Final
    @Mutable
    private Map<BlockPos, BlockEntity> blockEntities =  ConcurrentCollections.newHashMap();

}
