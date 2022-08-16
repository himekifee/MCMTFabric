package net.himeki.mcmtfabric.syncfu;

import com.llamalad7.mixinextras.MixinExtrasBootstrap;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SyncFuTransformer implements PreLaunchEntrypoint {
    private static final Logger syncFuTransformerLogger = LogManager.getLogger();
    private boolean isActive = true;

    @Override
    public void onPreLaunch() {
        syncFuTransformerLogger.info("On SyncFuTransformer PreLaunch...");
        try {
            FabricLauncherBase.getLauncher().loadIntoTarget("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap");
            FabricLauncherBase.getLauncher().loadIntoTarget("it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet");
            FabricLauncherBase.getLauncher().loadIntoTarget("it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap");
            FabricLauncherBase.getLauncher().loadIntoTarget("it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap");
            FabricLauncherBase.getLauncher().loadIntoTarget("it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap");
            FabricLauncherBase.getLauncher().loadIntoTarget("it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet");
            FabricLauncherBase.getLauncher().loadIntoTarget("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$ValueIterator");
            FabricLauncherBase.getLauncher().loadIntoTarget("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$KeySet");
            FabricLauncherBase.getLauncher().loadIntoTarget("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$KeyIterator");
            FabricLauncherBase.getLauncher().loadIntoTarget("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$MapEntrySet");
            FabricLauncherBase.getLauncher().loadIntoTarget("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$EntryIterator");
            FabricLauncherBase.getLauncher().loadIntoTarget("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$MapIterator");
            FabricLauncherBase.getLauncher().loadIntoTarget("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$MapEntry");
            FabricLauncherBase.getLauncher().loadIntoTarget("it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap$FastEntryIterator");
            FabricLauncherBase.getLauncher().loadIntoTarget("it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap$MapIterator");
//            FabricLauncherBase..getLauncher().loadIntoTarget("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$FastEntryIterator");  Error, is a interface
//            FabricLauncherBase..getLauncher().loadIntoTarget("it.unimi.dsi.fastutil.longs.Long2ObjectMap$FastEntrySet");
            MixinExtrasBootstrap.init();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
