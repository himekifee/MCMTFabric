package net.himeki.mcmtfabric.mixin;

import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.locks.ReentrantLock;

@Mixin(PalettedContainer.class)
public abstract class PalettedContainerMixin<T> {
    @Redirect(method = "lock", at = @At(value = "INVOKE", target = "Ljava/util/concurrent/locks/ReentrantLock;isHeldByCurrentThread()Z"))
    private boolean overwriteIsHeldByCurrentThread(ReentrantLock reentrantLock) {
        return true;
    }
}
