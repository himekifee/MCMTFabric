package net.himeki.mcmtfabric.jmx;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.himeki.mcmtfabric.DebugHookTerminator;
import net.minecraft.util.math.ChunkPos;

import javax.management.AttributeChangeNotification;
import javax.management.MBeanNotificationInfo;
import javax.management.NotificationBroadcasterSupport;

/**
 * MBean for debugging modlaunching issues
 *
 * @author jediminer543
 */
public class MCMTDebug extends NotificationBroadcasterSupport implements MCMTDebugMBean {

    ClassLoader ccl = null;

    public MCMTDebug() {
        // needed because classloading issues; classload all TRANSFORMER classes before stuff
        ccl = Thread.currentThread().getContextClassLoader();
    }

    @Override
    public String[] getLoadedMods() {
        Thread.currentThread().setContextClassLoader(ccl);
        return FabricLoader.getInstance().getAllMods().stream().map(ModContainer::getMetadata).map(info -> info.getId() + ":" + info.getVersion().toString()).toArray(String[]::new);
    }

    @Override
    public String getMainChunkLoadStatus() {
        Thread.currentThread().setContextClassLoader(ccl);
        return DebugHookTerminator.mainThreadChunkLoad.get() + ":" + DebugHookTerminator.mainThreadChunkLoadCount.get();
    }

    @Override
    public String[] getBrokenChunkList() {
        Thread.currentThread().setContextClassLoader(ccl);
        return DebugHookTerminator.breaks.stream().map(bcl -> new ChunkPos(bcl.getChunkPos()).toString()).toArray(String[]::new);
    }

    @Override
    public MBeanNotificationInfo[] getNotificationInfo() {
        String[] types = new String[]{
                AttributeChangeNotification.ATTRIBUTE_CHANGE
        };

        String name = AttributeChangeNotification.class.getName();
        String description = "An attribute of this MBean has changed";
        MBeanNotificationInfo info =
                new MBeanNotificationInfo(types, name, description);
        return new MBeanNotificationInfo[]{info};
    }

}
