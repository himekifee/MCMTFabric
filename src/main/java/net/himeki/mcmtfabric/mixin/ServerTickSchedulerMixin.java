package net.himeki.mcmtfabric.mixin;

import net.himeki.mcmtfabric.ParallelProcessor;
import net.himeki.mcmtfabric.parallelised.ConcurrentCollections;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.TickScheduler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;

@Mixin(ServerTickScheduler.class)
public abstract class ServerTickSchedulerMixin<T> implements TickScheduler<T> {
    @Shadow
    Set<ScheduledTick<T>> scheduledTickActionsInOrder;

    @Shadow
    @Final
    private Set<ScheduledTick<T>> scheduledTickActions = ConcurrentCollections.newHashSet();

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Set;size()I"))
    private int onSkipThrow(Set set) {
        return this.scheduledTickActionsInOrder.size();
    }

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Set;iterator()Ljava/util/Iterator;"))
    private void onFixStl(CallbackInfo ci) {
        if (scheduledTickActionsInOrder.size() != scheduledTickActions.size())
            ParallelProcessor.fixSTL((ServerTickScheduler<T>) (Object) this, scheduledTickActionsInOrder, scheduledTickActions);
    }
}
