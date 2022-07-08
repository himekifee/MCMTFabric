package net.himeki.mcmtfabric.mixin;

import it.unimi.dsi.fastutil.shorts.ShortSet;
import net.himeki.mcmtfabric.parallelised.fastutil.ConcurrentShortHashSet;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.light.LightingProvider;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkHolder.class)
public abstract class ChunkHolderMixin {

    @Mutable
    @Shadow
    @Final
    private ShortSet[] blockUpdatesBySection;

    @Inject(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ChunkHolder;blockUpdatesBySection:[Lit/unimi/dsi/fastutil/shorts/ShortSet;", opcode = Opcodes.PUTFIELD, shift = At.Shift.AFTER))
    private void overwriteShortSet(ChunkPos pos, int level, HeightLimitView world, LightingProvider lightingProvider, ChunkHolder.LevelUpdateListener levelUpdateListener, ChunkHolder.PlayersWatchingChunkProvider playersWatchingChunkProvider, CallbackInfo ci) {
        this.blockUpdatesBySection = new ConcurrentShortHashSet[world.countVerticalSections()];
    }

    @Redirect(method = "markForBlockUpdate", at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ChunkHolder;blockUpdatesBySection:[Lit/unimi/dsi/fastutil/shorts/ShortSet;", args = "array=set"))
    private void setBlockUpdatesBySection(ShortSet[] array, int index, ShortSet value) {
        array[index] = new ConcurrentShortHashSet();
    }
}
