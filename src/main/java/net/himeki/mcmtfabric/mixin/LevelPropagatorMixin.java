package net.himeki.mcmtfabric.mixin;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import net.himeki.mcmtfabric.parallelised.fastutil.Long2ByteConcurrentHashMap;
import net.himeki.mcmtfabric.parallelised.fastutil.sync.SyncLongLinkedOpenHashSet;
import net.minecraft.world.chunk.light.LevelPropagator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelPropagator.class)
public abstract class LevelPropagatorMixin {
    @Shadow
    @Final
    private Long2ByteMap pendingUpdates = new Long2ByteConcurrentHashMap();

    @Shadow
    @Mutable
    private LongLinkedOpenHashSet[] pendingIdUpdatesByLevel;


    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/chunk/light/LevelPropagator;pendingIdUpdatesByLevel:[Lit/unimi/dsi/fastutil/longs/LongLinkedOpenHashSet;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void handle(int levelCount, int expectedLevelSize, int expectedTotalSize, CallbackInfo ci) {
        pendingIdUpdatesByLevel = new SyncLongLinkedOpenHashSet[levelCount];
    }

    @Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/world/chunk/light/LevelPropagator;pendingIdUpdatesByLevel:[Lit/unimi/dsi/fastutil/longs/LongLinkedOpenHashSet;", args = "array=set"))
    private void overwritePendingIdUpdatesByLevel(LongLinkedOpenHashSet[] hashSets, int index, LongLinkedOpenHashSet hashSet, int levelCount, final int expectedLevelSize, final int expectedTotalSize) {
        hashSets[index] = new SyncLongLinkedOpenHashSet(expectedLevelSize, 0.5f);
    }
}
