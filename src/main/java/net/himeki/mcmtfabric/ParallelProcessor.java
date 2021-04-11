package net.himeki.mcmtfabric;

import net.himeki.mcmtfabric.config.GeneralConfig;
import net.himeki.mcmtfabric.parallelised.ChunkLock;
import net.himeki.mcmtfabric.parallelised.RunnableManagedBlocker;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.PistonBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.BlockEventS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.BlockEvent;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Tickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ScheduledTick;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

public class ParallelProcessor {

    private static final Logger LOGGER = LogManager.getLogger();

    static Phaser p;
    static ExecutorService ex;
    static MinecraftServer mcs;
    static AtomicBoolean isTicking = new AtomicBoolean();
    static AtomicInteger threadID = new AtomicInteger();

    public static void setupThreadPool(int parallelism) {
        threadID = new AtomicInteger();
        ForkJoinPool.ForkJoinWorkerThreadFactory fjpf = p -> {
            ForkJoinWorkerThread fjwt = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(p);
            fjwt.setName("MCMT-Pool-Thread-" + threadID.getAndIncrement());
            return fjwt;
        };
        ex = new ForkJoinPool(
                parallelism,
                fjpf,
                null, true);
    }

    /**
     * Creates and sets up the thread pool
     */
    static {
        // Must be static here due to class loading shenanagins
        setupThreadPool(4);
    }

    static Map<String, Set<Thread>> mcThreadTracker = new ConcurrentHashMap<String, Set<Thread>>();

    // Statistics
    public static AtomicInteger currentWorlds = new AtomicInteger();
    public static AtomicInteger currentEnts = new AtomicInteger();
    public static AtomicInteger currentTEs = new AtomicInteger();
    public static AtomicInteger currentEnvs = new AtomicInteger();

    //Operation logging
    public static Set<String> currentTasks = ConcurrentHashMap.newKeySet();

    public static void regThread(String poolName, Thread thread) {
        mcThreadTracker.computeIfAbsent(poolName, s -> ConcurrentHashMap.newKeySet()).add(thread);
    }

    public static boolean isThreadPooled(String poolName, Thread t) {
        return mcThreadTracker.containsKey(poolName) && mcThreadTracker.get(poolName).contains(t);
    }

    public static void preTick(MinecraftServer server) {
        if (p != null) {
            LOGGER.warn("Multiple servers?");
            return;
        } else {
            isTicking.set(true);
            p = new Phaser();
            p.register();
            mcs = server;
        }
    }

    public static void callTick(ServerWorld serverworld, BooleanSupplier hasTimeLeft, MinecraftServer server) {
        if (GeneralConfig.disabled || GeneralConfig.disableWorld) {
            try {
                serverworld.tick(hasTimeLeft);
            } catch (Exception e) {
                throw e;
            }
            return;
        }
        if (mcs != server) {
            LOGGER.warn("Multiple servers?");
            GeneralConfig.disabled = true;
            serverworld.tick(hasTimeLeft);
            return;
        } else {
            String taskName = "WorldTick: " + serverworld.toString() + "@" + serverworld.hashCode();
            if (GeneralConfig.opsTracing) currentTasks.add(taskName);
            p.register();
            ex.execute(() -> {
                try {
                    currentWorlds.incrementAndGet();
                    serverworld.tick(hasTimeLeft);
                } finally {
                    p.arriveAndDeregister();
                    currentWorlds.decrementAndGet();
                    if (GeneralConfig.opsTracing) currentTasks.remove(taskName);
                }
            });
        }

    }

    public static void postTick(MinecraftServer server) {
        if (mcs != server) {
            LOGGER.warn("Multiple servers?");
            return;
        } else {
            p.arriveAndAwaitAdvance();
            isTicking.set(false);
            p = null;
        }
    }


    public static void callEntityTick(Entity entityIn) {
        if (GeneralConfig.disabled || GeneralConfig.disableEntity) {
            entityIn.tick();
            return;
        }
        String taskName = "EntityTick: " + entityIn.toString() + "@" + entityIn.hashCode();
        if (GeneralConfig.opsTracing) currentTasks.add(taskName);
        p.register();
        ex.execute(() -> {
            try {
                currentEnts.incrementAndGet();
                entityIn.tick();
            } finally {
                currentEnts.decrementAndGet();
                p.arriveAndDeregister();
                if (GeneralConfig.opsTracing) currentTasks.remove(taskName);
            }
        });
    }

    public static void callTickChunks(ServerWorld world, WorldChunk chunk, int k) {
        if (GeneralConfig.disabled || GeneralConfig.disableEnvironment) {
            world.tickChunk(chunk, k);
            return;
        }
        String taskName = "EnvTick: " + chunk.toString() + "@" + chunk.hashCode();
        if (GeneralConfig.opsTracing) currentTasks.add(taskName);
        p.register();
        ex.execute(() -> {
            try {
                currentEnvs.incrementAndGet();
                world.tickChunk(chunk, k);
            } finally {
                currentEnvs.decrementAndGet();
                p.arriveAndDeregister();
                if (GeneralConfig.opsTracing) currentTasks.remove(taskName);
            }
        });
    }

    public static boolean filterTE(Tickable tte) {
        boolean isLocking = false;
        if (GeneralConfig.teBlackList.contains(tte.getClass())) {
            isLocking = true;
        }
        // Apparently a string starts with check is faster than Class.getPackage; who knew (I didn't)
        if (!isLocking && GeneralConfig.chunkLockModded && !tte.getClass().getName().startsWith("net.minecraft.tileentity.")) {
            isLocking = true;
        }
        if (isLocking && GeneralConfig.teWhiteList.contains(tte.getClass())) {
            isLocking = false;
        }
        if (tte instanceof PistonBlockEntity) {
            isLocking = true;
        }
        return isLocking;
    }

    public static void callTileEntityTick(Tickable tte, World world) {
        if (GeneralConfig.disabled || GeneralConfig.disableTileEntity || !(world instanceof ServerWorld)) {
            tte.tick();
            return;
        }
        String taskName = "TETick: " + tte.toString() + "@" + tte.hashCode();
        if (GeneralConfig.opsTracing) currentTasks.add(taskName);
        p.register();
        ex.execute(() -> {
            try {
                final boolean doLock = filterTE(tte);
                if (doLock) {
                    ForkJoinPool.managedBlock(new RunnableManagedBlocker(() -> {
                        BlockPos bp = ((BlockEntity) tte).getPos();
                        long[] locks = ChunkLock.lock(bp, 1);
                        try {
                            currentTEs.incrementAndGet();
                            tte.tick();
                        } finally {
                            ChunkLock.unlock(locks);
                        }
                    }));
                } else {
                    currentTEs.incrementAndGet();
                    tte.tick();
                }
            } catch (Exception e) {
                System.err.println("Exception ticking TE at " + ((BlockEntity) tte).getPos());
                e.printStackTrace();
            } finally {
                currentTEs.decrementAndGet();
                p.arriveAndDeregister();
                if (GeneralConfig.opsTracing) currentTasks.remove(taskName);
            }
        });
    }

    public static <T> void fixSTL(ServerTickScheduler<T> stl, TreeSet<ScheduledTick<T>> scheduledTickActionsInOrder, Set<ScheduledTick<T>> scheduledTickActions) {
        LOGGER.debug("FixSTL Called");
        scheduledTickActionsInOrder.addAll(scheduledTickActions);
    }

    public static void sendQueuedBlockEvents(Deque<BlockEvent> d, ServerWorld sw) {
        Iterator<BlockEvent> bed = d.iterator();
        while (bed.hasNext()) {
            BlockEvent BlockEvent = bed.next();
            if (sw.processBlockEvent(BlockEvent)) {
                /* 1.16.1 code; AKA the only thing that changed  */
                sw.getServer().getPlayerManager().sendToAround(null, BlockEvent.getPos().getX(), BlockEvent.getPos().getY(), BlockEvent.getPos().getZ(), 64.0D, sw.getRegistryKey(), new BlockEventS2CPacket(BlockEvent.getPos(), BlockEvent.getBlock(), BlockEvent.getType(), BlockEvent.getData()));
                /* */
				/* 1.15.2 code; AKA the only thing that changed
				sw.getServer().getPlayerList().sendToAllNearExcept((PlayerEntity)null, (double)BlockEvent.getPosition().getX(), (double)BlockEvent.getPosition().getY(), (double)BlockEvent.getPosition().getZ(), 64.0D, sw.getDimension().getType(), new SBlockActionPacket(BlockEvent.getPosition(), BlockEvent.getBlock(), BlockEvent.getEventID(), BlockEvent.getEventParameter()));
				/* */
            }
            if (!isTicking.get()) {
                LOGGER.fatal("Block updates outside of tick");
            }
            bed.remove();
        }
    }
}
