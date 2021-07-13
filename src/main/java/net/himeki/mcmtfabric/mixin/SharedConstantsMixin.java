package net.himeki.mcmtfabric.mixin;

import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SharedConstants.class)
public abstract class SharedConstantsMixin {
    @Shadow
    public static boolean isDevelopment = true;
}
