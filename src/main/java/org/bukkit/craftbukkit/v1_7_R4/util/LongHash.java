package org.bukkit.craftbukkit.v1_7_R4.util;

public class LongHash {
    public static long toLong(int msw, int lsw) {
        return ((long) msw << 32) + lsw - Integer.MIN_VALUE;
    }

    public static int msw(long l) {
        return (int) (l >> 32);
    }

    public static int lsw(long l) {
        return (int) (l) + Integer.MIN_VALUE; // Spigot - remove redundant &
    }
}
