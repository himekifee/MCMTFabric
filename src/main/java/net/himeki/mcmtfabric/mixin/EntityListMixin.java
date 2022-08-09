package net.himeki.mcmtfabric.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.himeki.mcmtfabric.parallelised.fastutil.Int2ObjectConcurrentHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.world.EntityList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityList.class)
public abstract class EntityListMixin {
    @Shadow
    private Int2ObjectMap<Entity> entities = new Int2ObjectConcurrentHashMap<>();

    @Shadow
    private Int2ObjectMap<Entity> temp = new Int2ObjectConcurrentHashMap<>();

    @Inject(method = "ensureSafe", at = @At(value = "HEAD"), cancellable = true)
    private void notSafeAnyWay(CallbackInfo ci) {
        ci.cancel();
    }
}
