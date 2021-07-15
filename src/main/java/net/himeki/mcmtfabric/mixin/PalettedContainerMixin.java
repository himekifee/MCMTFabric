package net.himeki.mcmtfabric.mixin;

import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.concurrent.Semaphore;

@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin<T> {

    @Shadow
    @Final
    private final Semaphore writeLock = new Semaphore(255);
}
