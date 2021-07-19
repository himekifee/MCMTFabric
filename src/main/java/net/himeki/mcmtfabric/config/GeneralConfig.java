package net.himeki.mcmtfabric.config;

import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Config(name = "mcmtfabric")
public class GeneralConfig implements ConfigData {
    // Actual config stuff
    //////////////////////

    // General
    @Comment("Globally disable all toggleable functionality")
    public static boolean disabled = false;

    // Parallelism
    @Comment("Thread count config; In standard mode: will never create more threads than there are CPU threads (as that causeses Context switch churning)\n" +
            "Values <=1 are treated as 'all cores'")
    public static int paraMax = -1;

    @Comment("""
            Other modes for paraMax
            Override: Standard but without the CoreCount Ceiling (So you can have 64k threads if you want)
            Reduction: Parallelism becomes Math.max(CoreCount-paramax, 2), if paramax is set to be -1, it's treated as 0
            Todo: add more"""
    )
    public static ParaMaxMode paraMaxMode = ParaMaxMode.Standard;

    // World
    @Comment("Disable world parallelisation")
    public static boolean disableWorld = false;

    @Comment("Disable world post tick parallelisation")
    public static boolean disableWorldPostTick = false;

    // Entity
    @Comment("Disable entity parallelisation")
    public static boolean disableEntity = false;

    // TE
    @Comment("Disable tile entity parallelisation")
    public static boolean disableTileEntity = false;

    @Comment("Use chunklocks for any unknown (i.e. modded) tile entities\n"
            + "Chunklocking means we prevent multiple tile entities a 1 chunk radius of each other being ticked to limit concurrency impacts")
    public static boolean chunkLockModded = true;

    @Comment("""
            List of tile entity classes that will always be fully parallelised
            This will occur even when chunkLockModded is set to true
            Adding pistons to this will not parallelise them"""
    )
    @ConfigEntry.Gui.Excluded
    public static Set<Class<?>> teWhiteList = ConcurrentHashMap.newKeySet();
    public static List<String> teWhiteListString = new ArrayList<>();

    @Comment("List of tile entity classes that will always be chunklocked\n"
            + "This will occur even when chunkLockModded is set to false")
    @ConfigEntry.Gui.Excluded
    public static Set<Class<?>> teBlackList = ConcurrentHashMap.newKeySet();
    public static List<String> teBlackListString = new ArrayList<>();

    // Any TE class strings that aren't available in the current environment
    // We use classes for the main operation as class-class comparisons are memhash based
    // So (should) be MUCH faster than string-string comparisons
    @ConfigEntry.Gui.Excluded
    public static List<String> teUnfoundWhiteList = new ArrayList<>();
    @ConfigEntry.Gui.Excluded
    public static List<String> teUnfoundBlackList = new ArrayList<>();

    // Misc
    @Comment("Disable environment (plant ticks, etc.) parallelisation")
    public static boolean disableEnvironment = false;

    @Comment("Disable parallelised chunk caching; doing this will result in much lower performance with little to no gain")
    public static boolean disableChunkProvider = false;

    //Debug
    @Comment("Enable chunk loading timeouts; this will forcibly kill any chunks that fail to load in sufficient time\n"
            + "May allow for loading of damaged/corrupted worlds")
    public static boolean enableChunkTimeout = false;

    @Comment("Attempts to re-load timed out chunks; Seems to work")
    public static boolean enableTimeoutRegen = false;

    @Comment("Amount of workless iterations to wait before declaring a chunk load attempt as timed out\n"
            + "This is in ~100us iterations (plus minus yield time) so timeout >= timeoutCount * 100us")
    public static int timeoutCount = 5000;

    // More Debug
    @Comment("Enable ops tracing; this will probably have a performance impact, but allows for better debugging")
    public static boolean opsTracing = false;

    @Comment("Maximum time between MCMT presence alerts in 10ms steps")
    public static int logCap = 720000;


    public enum ParaMaxMode {
        Standard,
        Override,
        Reduction
    }

    // Functions intended for usage
    ///////////////////////////////

    @Override
    public void validatePostLoad() throws ValidationException {
        if (paraMax >= -1 && paraMax <= Integer.MAX_VALUE)
            if (paraMaxMode == ParaMaxMode.Standard || paraMaxMode == ParaMaxMode.Override || paraMaxMode == ParaMaxMode.Reduction)
                if (timeoutCount >= 500 && timeoutCount <= 500000)
                    if (logCap >= 15000 && logCap <= Integer.MAX_VALUE)
                        return;
                    throw new ValidationException("Failed to validate MCMT config.");
    }

    public static int getParallelism() {
        switch (GeneralConfig.paraMaxMode) {
            case Standard:
                return GeneralConfig.paraMax <= 1 ?
                        Runtime.getRuntime().availableProcessors() :
                        Math.max(2, Math.min(Runtime.getRuntime().availableProcessors(), GeneralConfig.paraMax));
            case Override:
                return GeneralConfig.paraMax <= 1 ?
                        Runtime.getRuntime().availableProcessors() :
                        Math.max(2, GeneralConfig.paraMax);
            case Reduction:
                return Math.max(
                        Runtime.getRuntime().availableProcessors() - Math.max(0, GeneralConfig.paraMax),
                        2);
        }
        // Unsure quite how this is "Reachable code" but ok I guess
        return Runtime.getRuntime().availableProcessors();
    }

    public GeneralConfig() {
        teWhiteListString.forEach(str -> {
            Class<?> c = null;
            try {
                c = Class.forName(str);
                teWhiteList.add(c);
            } catch (ClassNotFoundException cnfe) {
                teUnfoundWhiteList.add(str);
            }
        });

        teBlackListString.forEach(str -> {
            Class<?> c = null;
            try {
                c = Class.forName(str);
                teBlackList.add(c);
            } catch (ClassNotFoundException cnfe) {
                teUnfoundBlackList.add(str);
            }
        });

    }
}
