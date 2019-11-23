package com.destroystokyo.paper.utils;

public class SneakyThrow {
    public static void sneaky(Throwable exception) {
        SneakyThrow.<RuntimeException>throwSneaky(exception);
    }

    private static <T extends Throwable> void throwSneaky(Throwable exception) throws T {
        throw (T) exception;
    }
}
