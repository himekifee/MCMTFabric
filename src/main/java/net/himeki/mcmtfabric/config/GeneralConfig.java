package net.himeki.mcmtfabric.config;

import java.util.ArrayList;
import java.util.List;

/**
 * All defaults, for early stage programming
 */

public class GeneralConfig {
    public static boolean disabled = false;

    public static Integer paraMax = -1;
    public static ParaMaxMode paraMaxMode = ParaMaxMode.Standard;

    public static boolean disableWorld = false;
    public static boolean disableWorldPostTick = false;

    public static boolean disableEntity = false;

    public static boolean disableTileEntity = false;
    public static boolean chunkLockModded = true;
    public static List<String> teWhiteList = new ArrayList<String>();
    public static List<String> teBlackList = new ArrayList<String>();

    public static boolean disableEnvironment = false;

    public static boolean disableChunkProvider = false;
    public static boolean enableChunkTimeout = false;
    public static boolean enableTimeoutRegen = false;
    public static Integer timeoutCount = 5000;

    public static boolean opsTracing = true;

    public static enum ParaMaxMode {
        Standard,
        Override,
        Reduction
    }

}
