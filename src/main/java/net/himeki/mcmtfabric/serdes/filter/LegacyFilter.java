package net.himeki.mcmtfabric.serdes.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.himeki.mcmtfabric.MCMT;
import net.himeki.mcmtfabric.config.BlockEntityLists;
import net.himeki.mcmtfabric.config.GeneralConfig;
import net.himeki.mcmtfabric.serdes.ISerDesHookType;
import net.himeki.mcmtfabric.serdes.SerDesRegistry;
import net.himeki.mcmtfabric.serdes.pools.ChunkLockPool;
import net.himeki.mcmtfabric.serdes.pools.ISerDesPool;
import net.himeki.mcmtfabric.serdes.pools.ISerDesPool.ISerDesOptions;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class LegacyFilter implements ISerDesFilter {

    ISerDesPool clp;
    ISerDesOptions config;

    @Override
    public void init() {
        clp = SerDesRegistry.getOrCreatePool("LEGACY", ChunkLockPool::new);
        Map<String, Object> cfg = new HashMap<>();
        cfg.put("range", "1");
        config = clp.compileOptions(cfg);
    }

    @Override
    public void serialise(Runnable task, Object obj, BlockPos bp, World w, ISerDesHookType hookType) {
        clp.serialise(task, obj, bp, w, config);
    }

    @Override
    public Set<Class<?>> getTargets() {
        return BlockEntityLists.teBlackList;
    }

    @Override
    public Set<Class<?>> getWhitelist() {
        return BlockEntityLists.teWhiteList;
    }

}
