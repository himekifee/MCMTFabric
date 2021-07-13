package net.himeki.mcmtfabric.mixin;

import net.minecraft.world.entity.EntityIndex;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(EntityIndex.class)
public class EntityIndexMixin<T extends EntityLike> {

}
