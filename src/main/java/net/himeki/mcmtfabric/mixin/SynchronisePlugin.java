package net.himeki.mcmtfabric.mixin;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
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
import java.util.TreeSet;

public class SynchronisePlugin implements IMixinConfigPlugin {
    private static final Logger syncLogger = LogManager.getLogger();
    private final Multimap<String, String> mixin2MethodsMap = ArrayListMultimap.create();
    private final TreeSet<String> syncAllSet = new TreeSet();

    @Override
    public void onLoad(String mixinPackage) {
        MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.ServerChunkManagerMixin", mappingResolver.mapMethodName("intermediary", "net.minecraft.class_3215", "method_14161", "()V"));
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.ServerTickSchedulerMixin", mappingResolver.mapMethodName("intermediary", "net.minecraft.class_1949", "method_8670", "()V"));
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.ServerTickSchedulerMixin", mappingResolver.mapMethodName("intermediary", "net.minecraft.class_1949", "method_20514", "(Lnet/minecraft/class_1954;)V"));
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.ServerTickSchedulerMixin", mappingResolver.mapMethodName("intermediary", "net.minecraft.class_1949", "method_8672", "(Lnet/minecraft/class_3341;ZZ)Ljava/util/List;"));
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.ServerWorldMixin", mappingResolver.mapMethodName("intermediary", "net.minecraft.class_1937", "method_19282", "(Lnet/minecraft/class_2338;Lnet/minecraft/class_2680;Lnet/minecraft/class_2680;)V"));
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.LevelPropagatorMixin", mappingResolver.mapMethodName("intermediary", "net.minecraft.class_3554", "method_15492", "(I)I"));
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.LevelPropagatorMixin", mappingResolver.mapMethodName("intermediary", "net.minecraft.class_3554", "method_15478", "(JJIZ)V"));
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.SectionedEntityCacheMixin","forEachInBox");
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.SectionedEntityCacheMixin","addSection");
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.SectionedEntityCacheMixin","removeSection");
        mixin2MethodsMap.put("net.himeki.mcmtfabric.mixin.SectionedEntityCacheMixin","sectionCount");



        syncAllSet.add("net.himeki.mcmtfabric.mixin.fastutil.Int2ObjectOpenHashMapMixin");
        syncAllSet.add("net.himeki.mcmtfabric.mixin.fastutil.Long2ObjectOpenHashMapMixin");
        syncAllSet.add("net.himeki.mcmtfabric.mixin.fastutil.LongLinkedOpenHashSetMixin");
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
                        syncLogger.info("Setting synchronize bit for " + method.name + " in " + targetClassName + ".");
                    }
            }
        else if (syncAllSet.contains(mixinClassName)) {
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
