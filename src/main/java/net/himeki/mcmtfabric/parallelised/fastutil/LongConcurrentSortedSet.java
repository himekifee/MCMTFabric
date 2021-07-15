package net.himeki.mcmtfabric.parallelised.fastutil;

import it.unimi.dsi.fastutil.longs.LongBidirectionalIterator;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongComparator;
import it.unimi.dsi.fastutil.longs.LongSortedSet;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.ConcurrentSkipListSet;

public class LongConcurrentSortedSet implements LongSortedSet {
    ConcurrentSkipListSet<Long> backing;

    public LongConcurrentSortedSet() {
        this.backing = new ConcurrentSkipListSet<>();
    }

    @Override
    public LongBidirectionalIterator iterator(long fromElement) {
        return null;
    }

    @Override
    public int size() {
        return backing.size();
    }

    @Override
    public boolean isEmpty() {
        return backing.isEmpty();
    }

    @Override
    public LongBidirectionalIterator iterator() {
        return new LongBidirectionalIterator() {
            Long now = backing.first();

            @Override
            public long previousLong() {
                if (hasPrevious()) {
                    now = backing.floor(now);
                    return now;
                } else return -1;
            }

            @Override
            public boolean hasPrevious() {
                return !now.equals(backing.first());
            }

            @Override
            public long nextLong() {
                if (hasNext()) {
                    now = backing.ceiling(now);
                    return now;
                } else return -1;
            }

            @Override
            public boolean hasNext() {
                return !now.equals(backing.last());
            }
        };
    }

    @NotNull
    @Override
    public Object[] toArray() {
        return backing.toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] ts) {
        return null;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> collection) {
        return backing.containsAll(collection);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends Long> collection) {
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
    public boolean add(long key) {
        return backing.add(key);
    }

    @Override
    public boolean contains(long key) {
        return backing.contains(key);
    }

    @Override
    public long[] toLongArray() {
        return new long[0];
    }

    @Override
    public long[] toLongArray(long[] a) {
        return new long[0];
    }

    @Override
    public long[] toArray(long[] a) {
        return new long[0];
    }

    @Override
    public boolean addAll(LongCollection c) {
        return backing.addAll(c);
    }

    @Override
    public boolean containsAll(LongCollection c) {
        return backing.containsAll(c);
    }

    @Override
    public boolean removeAll(LongCollection c) {
        return backing.removeAll(c);
    }

    @Override
    public boolean retainAll(LongCollection c) {
        return backing.retainAll(c);
    }

    @Override
    public boolean remove(long k) {
        return backing.remove(k);
    }

    @Override
    public LongSortedSet subSet(long fromElement, long toElement) {
        LongConcurrentSortedSet newSet = new LongConcurrentSortedSet();
        newSet.addAll(backing.subSet(fromElement, toElement));
        return newSet;
    }

    @Override
    public LongSortedSet headSet(long toElement) {
        LongConcurrentSortedSet newSet = new LongConcurrentSortedSet();
        newSet.addAll(backing.headSet(toElement));
        return newSet;
    }

    @Override
    public LongSortedSet tailSet(long fromElement) {
        LongConcurrentSortedSet newSet = new LongConcurrentSortedSet();
        newSet.addAll(backing.tailSet(fromElement));
        return newSet;
    }

    @Override
    public LongComparator comparator() {
        return new LongComparator() {
            @Override
            public int compare(long k1, long k2) {
                return backing.comparator().compare(k1, k2);
            }
        };
    }

    @Override
    public long firstLong() {
        return backing.first();
    }

    @Override
    public long lastLong() {
        return backing.last();
    }
}
