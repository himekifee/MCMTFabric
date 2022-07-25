package net.himeki.mcmtfabric.mixin;

import net.minecraft.world.block.ChainRestrictedNeighborUpdater;
import net.minecraft.world.block.NeighborUpdater;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Mixin(ChainRestrictedNeighborUpdater.class)
public abstract class ChainRestrictedNeighborUpdaterMixin implements NeighborUpdater {

    @Shadow
    @Final
    @Mutable
    List<ChainRestrictedNeighborUpdater.Entry> pending = new CopyOnWriteArrayList<>();


}
