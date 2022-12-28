package net.himeki.mcmtfabric.parallelised.fastutil;

import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentShortHashSet implements ShortSet {

    ConcurrentHashMap.KeySetView<Short, Boolean> backing = ConcurrentHashMap.newKeySet();

    @Override
    public int size() {
        return backing.size();
    }

    @Override
    public boolean isEmpty() {
        return backing.isEmpty();
    }

    @Override
    public ShortIterator iterator() {
        return new FastUtilHackUtil.WrappingShortIterator(backing.iterator());
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return backing.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] ts) {
        return (T[]) backing.toArray();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        return backing.containsAll(collection);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Short> collection) {
        return backing.addAll(collection);
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> collection) {
        return backing.removeAll(collection);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> collection) {
        return backing.retainAll(collection);
    }

    @Override
    public void clear() {
        backing.clear();

    }

    @Override
    public boolean add(short key) {
        return backing.add(key);
    }

    @Override
    public boolean contains(short key) {
        return backing.contains(key);
    }

    @Override
    public short[] toShortArray() {
        return new short[0];
    }

    @Override
    public short[] toArray(short[] a) {
        return new short[0];
    }

    @Override
    public boolean addAll(ShortCollection c) {
        return backing.addAll(c);
    }

    @Override
    public boolean containsAll(ShortCollection c) {
        return backing.containsAll(c);
    }

    @Override
    public boolean removeAll(ShortCollection c) {
        return backing.removeAll(c);
    }

    @Override
    public boolean retainAll(ShortCollection c) {
        return backing.retainAll(c);
    }

    @Override
    public boolean remove(short k) {
        return backing.remove(k);
    }
}
