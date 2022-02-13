package net.himeki.mcmtfabric.jmx;

public interface MCMTDebugMBean {

    public String[] getLoadedMods();

    public String getMainChunkLoadStatus();

    public String[] getBrokenChunkList();

}
