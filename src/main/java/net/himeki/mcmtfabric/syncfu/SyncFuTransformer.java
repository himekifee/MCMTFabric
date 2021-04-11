package net.himeki.mcmtfabric.syncfu;

import net.devtech.grossfabrichacks.entrypoints.PrePreLaunch;
import net.devtech.grossfabrichacks.instrumentation.InstrumentationApi;
import net.devtech.grossfabrichacks.jarboot.JarBooter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;

public class SyncFuTransformer implements PrePreLaunch {
    private static final Logger syncFuTransformerLogger = LogManager.getLogger();
    private boolean isActive = true;

    @Override
    public void onPrePreLaunch() {

        Optional<URL> fuJarUrl = Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator)).flatMap(path -> {
            File file = new File(path);
            if (file.isDirectory()) {
                return Arrays.stream(file.list((d, n) -> n.endsWith(".jar")));
            }
            return Arrays.stream(new String[]{path});
        })
                .filter(p -> p.contains("fastutil")) // Can add more if necesary;
                .map(Paths::get)
                .map(path -> {
                    try {
                        return path.toUri().toURL();
                    } catch (Exception e) {
                        return null;
                    }
                }).findFirst();
        if (fuJarUrl.isPresent()) {
            syncFuTransformerLogger.info("Sync_Fu found fu...");
            if (isActive) {
                JarBooter.addUrl(fuJarUrl.get());
                InstrumentationApi.pipeClassThroughTransformerBootstrap("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap");
                InstrumentationApi.pipeClassThroughTransformerBootstrap("it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet");
                InstrumentationApi.pipeClassThroughTransformerBootstrap("it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap");
            }
        } else syncFuTransformerLogger.warn("Failed to find FastUtil jar; this WILL result in more exceptions");
    }
}
