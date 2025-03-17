package io.github.crucible.util;

public class BlockCoords {
    private static final int PACKED_X_LENGTH = 26;
    private static final int PACKED_Z_LENGTH = PACKED_X_LENGTH;
    private static final int PACKED_Y_LENGTH = 64 - PACKED_X_LENGTH - PACKED_Z_LENGTH;
    private static final long PACKED_X_MASK = (1L << PACKED_X_LENGTH) - 1L;
    private static final long PACKED_Y_MASK = (1L << PACKED_Y_LENGTH) - 1L;
    private static final long PACKED_Z_MASK = (1L << PACKED_Z_LENGTH) - 1L;
    private static final int Z_OFFSET = PACKED_Y_LENGTH;
    private static final int X_OFFSET = PACKED_Y_LENGTH + PACKED_Z_LENGTH;

    public final int x, y, z;
    public final long key;
    private final int hash;

    public BlockCoords(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;

        key = ((long) y << 56) | (((long) z & 0xFFFFFFF) << 28) | (x & 0xFFFFFFF);
        hash = Long.hashCode(key);
    }

    public BlockCoords(BlockCoords coords) {
        this.x = coords.x;
        this.y = coords.y;
        this.z = coords.z;
        this.key = coords.key;
        this.hash = coords.hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;
        if (!(obj instanceof BlockCoords))
            return false;
        BlockCoords coords = (BlockCoords) obj;
        return x == coords.x && y == coords.y && z == coords.z;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    public static int getX(long packedPos) {
        return (int)(packedPos << 64 - X_OFFSET - PACKED_X_LENGTH >> 64 - PACKED_X_LENGTH);
    }

    public static int getY(long packedPos) {
        return (int)(packedPos << 64 - PACKED_Y_LENGTH >> 64 - PACKED_Y_LENGTH);
    }

    public static int getZ(long packedPos) {
        return (int)(packedPos << 64 - Z_OFFSET - PACKED_Z_LENGTH >> 64 - PACKED_Z_LENGTH);
    }

    public static long asLong(int x, int y, int z) {
        long packedPos = 0L;
        packedPos |= ((long)x & PACKED_X_MASK) << X_OFFSET;
        packedPos |= ((long) y & PACKED_Y_MASK);
        return packedPos | ((long)z & PACKED_Z_MASK) << Z_OFFSET;
    }
}
