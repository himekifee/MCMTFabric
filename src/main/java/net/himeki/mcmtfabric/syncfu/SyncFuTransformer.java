package net.himeki.mcmtfabric.syncfu;


import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import net.devtech.grossfabrichacks.entrypoints.PrePreLaunch;
import net.devtech.grossfabrichacks.instrumentation.InstrumentationApi;
import net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public class SyncFuTransformer implements PrePreLaunch {
    @Override
    public void onPrePreLaunch() {
//        int posFilter = Opcodes.ACC_PUBLIC;
//        int negFilter = Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_BRIDGE;
//
//        AsmClassTransformer synchronizeTransformer = (name, classNode) -> {
//            for (MethodNode mn : classNode.methods) {
//                if ((mn.access & posFilter) == posFilter
//                        && (mn.access & negFilter) == 0
//                        && !mn.name.equals("<init>")) {
//                    mn.access |= Opcodes.ACC_SYNCHRONIZED;
//                    syncFuLogger.info("Setting synchronize bit for " + mn.name + " in " + classNode.name + ".");
//                }
//            }
//        };
        InstrumentationApi.pipeClassThroughTransformerBootstrap("it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap");
        InstrumentationApi.pipeClassThroughTransformerBootstrap("it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet");
        InstrumentationApi.pipeClassThroughTransformerBootstrap("it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap");

    }
}
