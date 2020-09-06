package org.bukkit.craftbukkit.util;

import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;

public class LazyPlayerSet extends LazyHashSet<Player> {

    @Override
    HashSet<Player> makeReference() {
        if (reference != null) {
            throw new IllegalStateException("Reference already created!");
        }
        List<net.minecraft.entity.player.EntityPlayerMP> players = net.minecraft.server.MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        HashSet<Player> reference = new HashSet<Player>(players.size());
        for (net.minecraft.entity.player.EntityPlayerMP player : players) {
            reference.add(player.getBukkitEntity());
        }
        return reference;
    }

}
