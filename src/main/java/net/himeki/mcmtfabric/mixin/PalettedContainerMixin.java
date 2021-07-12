package net.himeki.mcmtfabric.mixin;

import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin<T> {
}
