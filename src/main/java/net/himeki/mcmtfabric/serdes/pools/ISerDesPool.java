package net.himeki.mcmtfabric.serdes.pools;

import java.util.Map;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public interface ISerDesPool {

    public interface ISerDesOptions {
    }

    public void serialise(Runnable task, Object o, BlockPos bp, World w, @Nullable ISerDesOptions options);

    public default ISerDesOptions compileOptions(Map<String, Object> config) {
        return null;
    }

    public default void init(String name, Map<String, Object> config) {

    }

}