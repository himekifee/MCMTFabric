package net.himeki.mcmtfabric.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.util.Either;
import net.himeki.mcmtfabric.DebugHookTerminator;
import net.himeki.mcmtfabric.ParallelProcessor;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Mixin(ServerChunkManager.class)
public abstract class ServerChunkManagerMixin extends ChunkManager {

    @Shadow
    @Final
    public ServerChunkManager.MainThreadExecutor mainThreadExecutor;

    @Shadow
    @Final
    ServerWorld world;

    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Ljava/util/Collections;shuffle(Ljava/util/List;)V"))
    private void preChunkTick(List<?> list) {
        ParallelProcessor.preChunkTick(list.size(), this.world);
        Collections.shuffle(list);
    }

    @Redirect(method = "tickChunks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;tickChunk(Lnet/minecraft/world/chunk/WorldChunk;I)V"))
    private void overwriteTickChunk(ServerWorld serverWorld, WorldChunk chunk, int randomTickSpeed) {
        ParallelProcessor.callTickChunks(serverWorld, chunk, randomTickSpeed);
    }

    @ModifyExpressionValue(
            method = "tickChunks",
            at = {
                    @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;shouldTick(Lnet/minecraft/util/math/ChunkPos;)Z"),
                    @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;shouldTick(Lnet/minecraft/util/math/ChunkPos;)Z"),
                    @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;shouldTickBlocksInChunk(J)Z")
            }
    )
    private boolean continueToArrivePhaser(boolean shouldTick) {
        if (!shouldTick) {
            ParallelProcessor.arriveChunkPhaser(world);
        }
        return shouldTick;
    }

    @Redirect(method = {"getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", "getWorldChunk"}, at = @At(value = "FIELD", target = "Lnet/minecraft/server/world/ServerChunkManager;serverThread:Ljava/lang/Thread;", opcode = Opcodes.GETFIELD))
    private Thread overwriteServerThread(ServerChunkManager mgr) {
        return Thread.currentThread();
    }

    @Redirect(method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;visit(Ljava/lang/String;)V"))
    private void overwriteProfilerVisit(Profiler instance, String s) {
        if (ParallelProcessor.shouldThreadChunks())
            return;
        else instance.visit("getChunkCacheMiss");
    }

    @Inject(method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerChunkManager$MainThreadExecutor;runTasks(Ljava/util/function/BooleanSupplier;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void callCompletableFutureHook(int x, int z, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<Chunk> cir, Profiler profiler, long chunkPos, CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> i) {
        DebugHookTerminator.chunkLoadDrive(this.mainThreadExecutor, i::isDone, (ServerChunkManager) (Object) this, i, chunkPos);

    }

}