package com.destroystokyo.paper.util;

public class SneakyThrow {
    public static void sneaky(Throwable exception) {
        SneakyThrow.<RuntimeException>throwSneaky(exception);
    }

    private static <T extends Throwable> void throwSneaky(Throwable exception) throws T {
        throw (T) exception;
    }
}
