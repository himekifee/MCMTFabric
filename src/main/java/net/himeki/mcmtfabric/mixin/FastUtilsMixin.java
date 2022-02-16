package net.himeki.mcmtfabric.mixin;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {Int2ObjectOpenHashMap.class, Long2ObjectOpenHashMap.class, LongLinkedOpenHashSet.class, ObjectOpenCustomHashSet.class, Long2LongOpenHashMap.class, Long2ObjectLinkedOpenHashMap.class},
        targets = {"it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$ValueIterator", "it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$KeySet", "it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$KeyIterator",
                "it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$MapEntrySet", "it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$EntryIterator", "it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$MapIterator",
                "it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap$MapEntry", "it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap$FastEntryIterator",  "it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap$MapIterator"
})
public class FastUtilsMixin {
}
