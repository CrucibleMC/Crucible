package io.github.crucible.util;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;

import java.lang.reflect.Field;

public class Stringify {

    public static String describePacket(Packet packet) {
        StringBuilder sb = new StringBuilder();
        sb.append(packet.getClass().getName()).append("{\n");
        for (Field field : packet.getClass().getDeclaredFields()) {
            try {
                field.setAccessible(true);
                sb.append(" [").append(field.getName()).append("@").append(field.getType().getName()).append("] ")
                        .append(field.get(packet));
            } catch (IllegalAccessException e) {
                sb.append("  Error getting field ").append(field.getName()).append(": ").append(e.getMessage());
            }
            sb.append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
