package net.himeki.mcmtfabric.parallelised;

import com.mojang.datafixers.DataFixer;
import net.himeki.mcmtfabric.ParallelProcessor;
import net.himeki.mcmtfabric.config.GeneralConfig;
import net.minecraft.server.WorldGenerationProgressListener;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureManager;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ChunkStatusChangeListener;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.level.storage.LevelStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/* 1.16.1 code; AKA the only thing that changed  */
//import net.minecraft.world.storage.SaveFormat.LevelSave;
/* */

/* 1.15.2 code; AKA the only thing that changed
import java.io.File;
/* */

public class ParaServerChunkProvider extends ServerChunkManager {

    protected Map<ChunkCacheAddress, ChunkCacheLine> chunkCache = new ConcurrentHashMap<ChunkCacheAddress, ChunkCacheLine>();
    protected AtomicInteger access = new AtomicInteger(Integer.MIN_VALUE);
    protected static final int CACHE_SIZE = 512;
    protected Thread cacheThread;
    Logger log = LogManager.getLogger();
    Marker chunkCleaner = MarkerManager.getMarker("ChunkCleaner");
    private final World world;

    /* 1.16.1 code; AKA the only thing that changed  */
    public ParaServerChunkProvider(ServerWorld serverWorld, LevelStorage.Session session, DataFixer dataFixer, StructureManager structureManager, Executor workerExecutor, ChunkGenerator chunkGenerator, int viewDistance, boolean bl, WorldGenerationProgressListener worldGenerationProgressListener, ChunkStatusChangeListener chunkStatusChangeListener, Supplier<PersistentStateManager> supplier) {
        super(serverWorld,session,dataFixer,structureManager,workerExecutor,chunkGenerator,viewDistance,bl,worldGenerationProgressListener, chunkStatusChangeListener,supplier);
        world = serverWorld;
        cacheThread = new Thread(this::chunkCacheCleanup, "Chunk Cache Cleaner " + serverWorld.getRegistryKey().getValue().getPath());
        cacheThread.start();
    }
    /* */

	/* 1.15.2 code; AKA the only thing that changed
	public ParaServerChunkProvider(ServerWorld worldIn, File worldDirectory, DataFixer dataFixer,
			TemplateManager templateManagerIn, Executor executorIn, ChunkGenerator<?> chunkGeneratorIn,
			int viewDistance, IChunkStatusListener p_i51537_8_, Supplier<DimensionSavedDataManager> p_i51537_9_) {
		super(worldIn, worldDirectory, dataFixer, templateManagerIn, executorIn, chunkGeneratorIn, viewDistance, p_i51537_8_,
				p_i51537_9_);
		cacheThread = new Thread(this::chunkCacheCleanup, "Chunk Cache Cleaner " + worldIn.dimension.getType().getId());
		cacheThread.start();
	}
	/* */

    @Override
    @Nullable
    public Chunk getChunk(int chunkX, int chunkZ, ChunkStatus requiredStatus, boolean load) {
        if (GeneralConfig.disabled || GeneralConfig.disableChunkProvider) {
            if (ParallelProcessor.isThreadPooled("Main", Thread.currentThread())) {
                return CompletableFuture.supplyAsync(() -> {
                    return this.getChunk(chunkX, chunkZ, requiredStatus, load);
                }, this.mainThreadExecutor).join();
            }
            return super.getChunk(chunkX, chunkZ, requiredStatus, load);
        }

        if (ParallelProcessor.isThreadPooled("Main", Thread.currentThread())) {
            return CompletableFuture.supplyAsync(() -> {
                return this.getChunk(chunkX, chunkZ, requiredStatus, load);
            }, this.mainThreadExecutor).join();
        }

        long i = ChunkPos.toLong(chunkX, chunkZ);

        Chunk c = lookupChunk(i, requiredStatus, false);
        if (c != null) {
            return c;
        }

        //log.debug("Missed chunk " + i + " on status "  + requiredStatus.toString());

        Chunk cl;
        synchronized (this) {
            cl = super.getChunk(chunkX, chunkZ, requiredStatus, load);
        }
        cacheChunk(i, cl, requiredStatus);
        return cl;
    }

    @SuppressWarnings("unused")
    private Chunk getChunkyThing(long chunkPos, ChunkStatus requiredStatus, boolean load) {
        Chunk cl;
        synchronized (this) {
            cl = super.getChunk(ChunkPos.getPackedX(chunkPos), ChunkPos.getPackedZ(chunkPos), requiredStatus, load);
        }
        return cl;
    }

    public Chunk lookupChunk(long chunkPos, ChunkStatus status, boolean compute) {
        int oldAccess = access.getAndIncrement();
        if (access.get() < oldAccess) {
            // Long Rollover so super rare
            chunkCache.clear();
            return null;
        }
        ChunkCacheLine ccl;
        ccl = chunkCache.get(new ChunkCacheAddress(chunkPos, status));
        if (ccl != null) {
            ccl.updateLastAccess();
            return ccl.getChunk();
        }
        return null;

    }

    public void cacheChunk(long chunkPos, Chunk chunk, ChunkStatus status) {
        long oldAccess = access.getAndIncrement();
        if (access.get() < oldAccess) {
            // Long Rollover so super rare
            chunkCache.clear();
        }
        ChunkCacheLine ccl;
        if ((ccl = chunkCache.get(new ChunkCacheAddress(chunkPos, status))) != null) {
            ccl.updateLastAccess();
            ccl.updateChunkRef(chunk);
        }
        ccl = new ChunkCacheLine(chunk);
        chunkCache.put(new ChunkCacheAddress(chunkPos, status), ccl);
    }

    public void chunkCacheCleanup() {
        while (world == null || world.getServer() == null) {
            log.debug(chunkCleaner, "ChunkCleaner Waiting for startup");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        while (world.getServer().isRunning()) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int size = chunkCache.size();
            if (size < CACHE_SIZE)
                continue;
            // System.out.println("CacheFill: " + size);
            long maxAccess = chunkCache.values().stream().mapToInt(ccl -> ccl.lastAccess).max().orElseGet(() -> access.get());
            long minAccess = chunkCache.values().stream().mapToInt(ccl -> ccl.lastAccess).min()
                    .orElseGet(() -> Integer.MIN_VALUE);
            long cutoff = minAccess + (long) ((maxAccess - minAccess) / ((float) size / ((float) CACHE_SIZE)));
            for (Entry<ChunkCacheAddress, ChunkCacheLine> l : chunkCache.entrySet()) {
                if (l.getValue().getLastAccess() < cutoff | l.getValue().getChunk() == null) {
                    chunkCache.remove(l.getKey());
                }
            }
        }
        log.debug(chunkCleaner, "ChunkCleaner terminating");
    }

    protected class ChunkCacheAddress {

        protected long chunk;
        protected ChunkStatus status;

        public ChunkCacheAddress(long chunk, ChunkStatus status) {
            super();
            this.chunk = chunk;
            this.status = status;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(chunk) ^ status.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ChunkCacheAddress) {
                if ((((ChunkCacheAddress) obj).chunk == chunk) && (((ChunkCacheAddress) obj).status.equals(status))) {
                    return true;
                }
            }
            return false;
        }
    }

    protected class ChunkCacheLine {
        WeakReference<Chunk> chunk;
        int lastAccess;

        public ChunkCacheLine(Chunk chunk) {
            this(chunk, access.get());
        }

        public ChunkCacheLine(Chunk chunk, int lastAccess) {
            this.chunk = new WeakReference<>(chunk);
            this.lastAccess = lastAccess;
        }

        public Chunk getChunk() {
            return chunk.get();
        }

        public int getLastAccess() {
            return lastAccess;
        }

        public void updateLastAccess() {
            lastAccess = access.get();
        }

        public void updateChunkRef(Chunk c) {
            if (chunk.get() == null) {
                chunk = new WeakReference<>(c);
            }
        }
    }
}
