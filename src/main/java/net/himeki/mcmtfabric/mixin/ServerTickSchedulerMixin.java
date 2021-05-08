package net.himeki.mcmtfabric.mixin;

import net.himeki.mcmtfabric.ParallelProcessor;
import net.himeki.mcmtfabric.parallelised.ConcurrentCollections;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.TickScheduler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@Mixin(ServerTickScheduler.class)
public abstract class ServerTickSchedulerMixin<T> implements TickScheduler<T> {
    private ConcurrentSkipListSet<ScheduledTick<T>> concurrentScheduledTickActionsInOrder;

    @Inject(method = "<init>", at = @At(value = "TAIL"))
    private void onInit(ServerWorld world, Predicate<T> invalidObjPredicate, Function<T, Identifier> idToName, Consumer<ScheduledTick<T>> consumer, CallbackInfo ci) {
        concurrentScheduledTickActionsInOrder = new ConcurrentSkipListSet<>(ScheduledTick.getComparator());
    }

    @Shadow
    @Final
    private Set<ScheduledTick<T>> scheduledTickActions = ConcurrentCollections.newHashSet();

    @Shadow
    @Nullable
    protected abstract List<ScheduledTick<T>> transferTicksInBounds(@Nullable List<ScheduledTick<T>> dst, Collection<ScheduledTick<T>> src, BlockBox bounds, boolean move);

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/TreeSet;size()I"))
    private int onReplaceSize(TreeSet treeSet) {
        return 1;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Set;size()I"))
    private int onSkipThrow(Set set) {
        return 1;
    }

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/TreeSet;iterator()Ljava/util/Iterator;"))
    private Iterator<ScheduledTick<T>> onReplaceIterator(TreeSet treeSet) {
        return concurrentScheduledTickActionsInOrder.iterator();
    }

    @Redirect(method = "getScheduledTicks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerTickScheduler;transferTicksInBounds(Ljava/util/List;Ljava/util/Collection;Lnet/minecraft/util/math/BlockBox;Z)Ljava/util/List;"))
    private List onReplaceInOrder(ServerTickScheduler serverTickScheduler, List<ScheduledTick<T>> dst, Collection<ScheduledTick<T>> src, BlockBox bounds, boolean move) {
        return this.transferTicksInBounds((List) null, this.concurrentScheduledTickActionsInOrder, bounds, move);
    }

    @Redirect(method = "addScheduledTick", at = @At(value = "INVOKE", target = "Ljava/util/TreeSet;add(Ljava/lang/Object;)Z"))
    private boolean onReplaceAddScheduledTick(TreeSet treeSet, T t) {
        return concurrentScheduledTickActionsInOrder.add((ScheduledTick<T>) t);
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;getChunkManager()Lnet/minecraft/server/world/ServerChunkManager;"))
    private void onFixStl(CallbackInfo ci) {
        if (concurrentScheduledTickActionsInOrder.size() != scheduledTickActions.size())
            ParallelProcessor.fixSTL((ServerTickScheduler<T>) (Object) this, concurrentScheduledTickActionsInOrder, scheduledTickActions);
    }
}
