package net.himeki.mcmtfabric.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.himeki.mcmtfabric.parallelised.ConcurrentCollections;
import net.himeki.mcmtfabric.parallelised.fastutil.Int2ObjectConcurrentHashMap;
import net.minecraft.world.entity.EntityIndex;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;
import java.util.UUID;

@Mixin(EntityIndex.class)
public class EntityIndexMixin<T extends EntityLike> {
    @Shadow
    @Final
    private final Int2ObjectMap<T> idToEntity = new Int2ObjectConcurrentHashMap();

    @Shadow
    @Final
    private final Map<UUID, T> uuidToEntity = ConcurrentCollections.newHashMap();

}
