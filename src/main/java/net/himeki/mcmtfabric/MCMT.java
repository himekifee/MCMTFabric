package net.himeki.mcmtfabric;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.himeki.mcmtfabric.commands.ConfigCommand;
import net.himeki.mcmtfabric.commands.StatsCommand;
import net.himeki.mcmtfabric.config.GeneralConfig;
import net.minecraft.util.ActionResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MCMT implements ModInitializer {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        LOGGER.info("Initializing MCMTFabric...");
        ConfigHolder<GeneralConfig> holder = AutoConfig.register(GeneralConfig.class, Toml4jConfigSerializer::new);
        holder.registerLoadListener((manager, data) -> {
            holder.getConfig().loadTELists();
            return ActionResult.SUCCESS;
        });
        holder.load();  // Load again to run loadTELists() handler


        StatsCommand.runDataThread();
        StatsCommand.resetAll();

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            ConfigCommand.register(dispatcher);
        });

        LOGGER.info("MCMT Setting up threadpool...");
        ParallelProcessor.setupThreadPool(GeneralConfig.getParallelism());
    }
}
