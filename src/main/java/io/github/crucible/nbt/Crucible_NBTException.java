package io.github.crucible.nbt;

import net.minecraft.nbt.NBTException;

public class Crucible_NBTException extends NBTException {

    public Crucible_NBTException(String message, String json, int parserIndex) {
        super(message + " at: " + slice(json, parserIndex));
    }

    private static String slice(String json, int parserIndex) {
        StringBuilder sb = new StringBuilder();
        int index = Math.min(json.length(), parserIndex);
        if (index > 35) {
            sb.append("...");
        }
        sb.append(json, Math.max(0, index - 35), index);
        sb.append("<--[HERE]");
        return sb.toString();
    }
}