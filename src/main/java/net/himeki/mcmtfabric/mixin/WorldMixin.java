package net.himeki.mcmtfabric.mixin;

import net.himeki.mcmtfabric.ParallelProcessor;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(World.class)
public abstract class WorldMixin implements WorldAccess, AutoCloseable {
    @Shadow
    @Final
    @Mutable
    private Thread thread;

    @Shadow
    @Final
    protected List<BlockEntityTickInvoker> blockEntityTickers;

    @Inject(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;"))
    private void postEntityPreBlockEntityTick(CallbackInfo ci) {
        if ((Object) this instanceof ServerWorld) {
            ServerWorld thisWorld = (ServerWorld) (Object) this;
            ParallelProcessor.postEntityTick(thisWorld);
            ParallelProcessor.preBlockEntityTick(this.blockEntityTickers.size(), thisWorld);
        }
    }

    @Redirect(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;isRemoved()Z"))
    private boolean continueToArrivePhaser0(BlockEntityTickInvoker instance) {
        boolean bl = instance.isRemoved();
        if ((Object) this instanceof ServerWorld) {
            if (bl) {
                ParallelProcessor.arriveBlockEntityPhaser((ServerWorld) (Object) this);
            }
        }
        return bl;
    }

    @Redirect(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;shouldTickBlockPos(Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean continueToArrivePhaser1(World instance, BlockPos pos) {
        boolean bl = instance.shouldTickBlockPos(pos);
        if ((Object) this instanceof ServerWorld) {
            if (!bl) {
                ParallelProcessor.arriveBlockEntityPhaser((ServerWorld) (Object) this);
            }
        }
        return bl;
    }

    @Inject(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;pop()V"))
    private void postBlockEntityTick(CallbackInfo ci) {
        if ((Object) this instanceof ServerWorld) {
            ServerWorld thisWorld = (ServerWorld) (Object) this;
            ParallelProcessor.postBlockEntityTick(thisWorld);
        }
    }

    @Redirect(method = "tickBlockEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/BlockEntityTickInvoker;tick()V"))
    private void overwriteBlockEntityTick(BlockEntityTickInvoker blockEntityTickInvoker) {
        ParallelProcessor.callBlockEntityTick(blockEntityTickInvoker, (World) (Object) this);
    }

    @Redirect(method = "getBlockEntity", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;currentThread()Ljava/lang/Thread;"))
    private Thread overwriteCurrentThread() {
        return this.thread;
    }
}