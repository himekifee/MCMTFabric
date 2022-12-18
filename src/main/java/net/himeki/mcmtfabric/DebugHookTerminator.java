package net.himeki.mcmtfabric;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.WorldChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.himeki.mcmtfabric.config.GeneralConfig;

import com.mojang.datafixers.util.Either;

/* 1.15.2 code; AKA the only thing that changed
import net.minecraft.world.biome.provider.SingleBiomeProviderSettings;
/* */

// TODO Should be renamed ChunkRepairHookTerminator (Note requres coremod edit)

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;


/**
 * Handles chunk forcing in scenarios where world corruption has occured
 *
 * @author jediminer543
 */
public class DebugHookTerminator {

    private static final Logger LOGGER = LogManager.getLogger();

    private static boolean bypassLoadTarget = false;

    public static class BrokenChunkLocator {
        long chunkPos;
        CompletableFuture<?> maincf;
        CompletableFuture<?> brokecf;

        public BrokenChunkLocator(long chunkPos, CompletableFuture<?> maincf, CompletableFuture<?> brokecf) {
            super();
            this.chunkPos = chunkPos;
            this.maincf = maincf;
            this.brokecf = brokecf;
        }

        public long getChunkPos() {
            return chunkPos;
        }

    }

    public static List<BrokenChunkLocator> breaks = new ArrayList<>();

    public static boolean isBypassLoadTarget() {
        return bypassLoadTarget;
    }

    public static AtomicBoolean mainThreadChunkLoad = new AtomicBoolean();
    public static AtomicLong mainThreadChunkLoadCount = new AtomicLong();
    public static String mainThread = "Server thread";

    public static void chunkLoadDrive(ServerChunkManager.MainThreadExecutor executor, BooleanSupplier isDone, ServerChunkManager scp, CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> completableFuture, long chunkpos) {
		/*
		if (!GeneralConfig.enableChunkTimeout) {
			bypassLoadTarget = false;
			executor.driveUntil(isDone);
			return;
		}
		*/
        GeneralConfig config = MCMT.config;
        int failcount = 0;
        if (Thread.currentThread().getName().equals(mainThread)) {
            mainThreadChunkLoadCount.set(0);
            mainThreadChunkLoad.set(true);
        }
        while (!isDone.getAsBoolean()) {
            if (!executor.runTask()) {
                if (isDone.getAsBoolean()) {
                    if (Thread.currentThread().getName().equals(mainThread)) {
                        mainThreadChunkLoad.set(false);
                    }
                    break;
                }
                // Nothing more to execute
                if (!config.enableChunkTimeout || failcount++ < config.timeoutCount) {
                    if (Thread.currentThread().getName().equals(mainThread)) {
                        mainThreadChunkLoadCount.incrementAndGet();
                    }
                    Thread.yield();
                    LockSupport.parkNanos("THE END IS ~~NEVER~~ LOADING", 100000L);
                } else {
                    LOGGER.error("", new TimeoutException("Error fetching chunk " + chunkpos));
                    bypassLoadTarget = true;
                    if (config.enableTimeoutRegen || config.enableBlankReturn) {

                        // TODO build a 1.15 version of this
                        if (config.enableBlankReturn) {
                            /* 1.16.1 code; AKA the only thing that changed  */
                            // Generate a new empty chunk
                            Chunk out = new WorldChunk(scp.getWorld(), new ChunkPos(chunkpos));
                            // SCIENCE
                            completableFuture.complete(Either.left(out));
                            /* */
							/* 1.15.2 code; AKA the only thing that changed
							// Generate a new empty chunk
							// Null is legal here as it's literally not used
							SingleBiomeProviderSettings sbps = new SingleBiomeProviderSettings(null);
							sbps.setBiome(Registry.BIOME.getOrDefault(null));
							BiomeProvider bp = new SingleBiomeProvider(sbps);
							Chunk out = new Chunk(scp.world, new ChunkPos(chunkpos),
									new BiomeContainer(new ChunkPos(chunkpos), bp));
							// SCIENCE
							completableFuture.complete(Either.left(out));
							/* */
                        } else {
                            try {
                                NbtCompound cnbt = scp.threadedAnvilChunkStorage.getNbt(new ChunkPos(chunkpos)).get().get();
                                if (cnbt != null) {
                                    ProtoChunk cp = ChunkSerializer.deserialize((ServerWorld) scp.getWorld(), scp.threadedAnvilChunkStorage.pointOfInterestStorage, new ChunkPos(chunkpos), cnbt);
                                    completableFuture.complete(Either.left(new WorldChunk((ServerWorld) scp.getWorld(), cp, null)));
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                            completableFuture.complete(ChunkHolder.UNLOADED_CHUNK);
                        }
                    } else {
                        System.err.println(completableFuture.toString());
                        ChunkHolder chunkholder = scp.getChunkHolder(chunkpos);
                        CompletableFuture<?> firstBroke = null;
                        for (ChunkStatus cs : ChunkStatus.createOrderedList()) {
                            CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> cf = chunkholder.getFutureFor(cs);
                            if (cf == ChunkHolder.UNLOADED_CHUNK_FUTURE) {
                                System.out.println("Status: " + cs.toString() + " is not yet loaded");
                            } else {
                                System.out.println("Status: " + cs.toString() + " is " + cf.toString());
                                if (firstBroke == null && !cf.toString().contains("Completed normally")) {
                                    firstBroke = cf;
                                }
                            }
                        }
                        breaks.add(new BrokenChunkLocator(chunkpos, completableFuture, firstBroke));
                        completableFuture.complete(Either.right(new ChunkHolder.Unloaded() {
                            @Override
                            public String toString() {
                                return "TIMEOUT";
                            }
                        }));
                    }
                }
            }
        }
    }

    public static void checkNull(Object o) {
        if (o == null) {
            System.out.println("Null warning:");
            new Throwable("Null trace").printStackTrace();
        }
    }
}
