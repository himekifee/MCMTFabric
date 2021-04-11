package net.himeki.mcmtfabric.mixin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.devtech.grossfabrichacks.transformer.asm.AsmClassTransformer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class SynchronisePlugin implements IMixinConfigPlugin {
    private static final Logger syncLogger = LogManager.getLogger();
    private final Multimap<String, String> mixin2MethodsMap = ArrayListMultimap.create();

    @Override
    public void onLoad(String mixinPackage) {
        MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.ServerChunkManagerMixin", mappingResolver.mapMethodName("named", "net.minecraft.server.world.ServerChunkManager", "tickChunks", "()V"));
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.ServerTickSchedulerMixin", mappingResolver.mapMethodName("named", "net.minecraft.server.world.ServerTickScheduler", "tick", "()V"));
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.ServerTickSchedulerMixin", mappingResolver.mapMethodName("named", "net.minecraft.server.world.ServerTickScheduler", "addScheduledTick", "(Lnet/minecraft/world/ScheduledTick;)V"));
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.ServerTickSchedulerMixin", mappingResolver.mapMethodName("named", "net.minecraft.server.world.ServerTickScheduler", "getScheduledTicks", "(Lnet/minecraft/util/math/BlockBox;ZZ)Ljava/util/List;"));
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.ServerWorldMixin", mappingResolver.mapMethodName("named", "net.minecraft.server.world.ServerWorld", "onBlockChanged", "Lnet/minecraft/server/world/ServerWorld;onBlockChanged(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/block/BlockState;)V"));
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.LevelPropagatorMixin", mappingResolver.mapMethodName("named", "net.minecraft.world.chunk.light.LevelPropagator", "applyPendingUpdates", "(I)I"));
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.LevelPropagatorMixin", mappingResolver.mapMethodName("named", "net.minecraft.world.chunk.light.LevelPropagator", "updateLevel", "(JJIZ)V"));
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        Collection<String> targetMethods = mixin2MethodsMap.get(mixinClassName);
        if (targetMethods.size() != 0)
            for (MethodNode method : targetClass.methods) {
                for (String targetMethod : targetMethods)
                    if (method.name.equals(targetMethod)) {
                        method.access |= Opcodes.ACC_SYNCHRONIZED;
                        syncLogger.info("Setting synchronise bit for " + method.name + " in " + targetClassName + ".");
                    }
            }
        else if (mixinClassName.contains("Int2ObjectOpenHashMap")) {
            int posFilter = Opcodes.ACC_PUBLIC;
            int negFilter = Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC | Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT | Opcodes.ACC_BRIDGE;

            for (MethodNode method : targetClass.methods) {
                if ((method.access & posFilter) == posFilter
                        && (method.access & negFilter) == 0
                        && !method.name.equals("<init>")) {
                    method.access |= Opcodes.ACC_SYNCHRONIZED;
                    syncLogger.info("Setting synchronize bit for " + method.name + " in " + targetClassName + ".");
                }
            }

        }
    }
}
