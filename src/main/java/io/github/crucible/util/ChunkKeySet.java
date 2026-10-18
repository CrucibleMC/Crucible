package io.github.crucible.util;

import net.minecraft.world.ChunkCoordIntPair;
import org.bukkit.craftbukkit.v1_7_R4.util.LongHash;
import org.bukkit.craftbukkit.v1_7_R4.util.LongHashSet;

import java.util.AbstractSet;
import java.util.Iterator;

/**
 * A set of chunk coordinates that is also a {@code Set<Long>} of vanilla chunk keys
 * ({@link ChunkCoordIntPair#chunkXZ2Int}), so code written against a Forge server's
 * {@code Set<Long>} sees the same type and the same keys.
 * <p>
 * The {@code (x, z)} overloads and {@link #popFirst()} skip boxing and work in {@link LongHash}
 * encoding, which is what Crucible's chunk maps are keyed by.
 */
public class ChunkKeySet extends AbstractSet<Long> {
    private final LongHashSet keys = new LongHashSet();

    public boolean add(int x, int z) {
        return keys.add(x, z);
    }

    public boolean contains(int x, int z) {
        return keys.contains(x, z);
    }

    public void remove(int x, int z) {
        keys.remove(x, z);
    }

    /** Removes one entry and returns its {@link LongHash} key; check {@link #isEmpty()} first. */
    public long popFirst() {
        return keys.popFirst();
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return keys.isEmpty();
    }

    @Override
    public void clear() {
        keys.clear();
    }

    @Override
    public boolean add(Long vanillaKey) {
        return keys.add(toLongHash(vanillaKey));
    }

    @Override
    public boolean contains(Object vanillaKey) {
        return vanillaKey instanceof Long && keys.contains(toLongHash((Long) vanillaKey));
    }

    @Override
    public boolean remove(Object vanillaKey) {
        return vanillaKey instanceof Long && keys.remove(toLongHash((Long) vanillaKey));
    }

    @Override
    public Iterator<Long> iterator() {
        @SuppressWarnings("unchecked")
        final Iterator<Long> it = keys.iterator();
        return new Iterator<Long>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public Long next() {
                long key = it.next();
                return ChunkCoordIntPair.chunkXZ2Int(LongHash.msw(key), LongHash.lsw(key));
            }

            @Override
            public void remove() {
                it.remove();
            }
        };
    }

    private static long toLongHash(long vanillaKey) {
        return LongHash.toLong((int) vanillaKey, (int) (vanillaKey >> 32));
    }
}
