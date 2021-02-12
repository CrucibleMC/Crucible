package io.github.crucible;

import io.github.crucible.util.ListSet;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import org.bukkit.scheduler.BukkitRunnable;

public class CrucibleCleaner extends BukkitRunnable {
    @Override
    public void run() {
        CrucibleModContainer.logger.info("Crucible is trying to free up resources, it may cause a small lag spike.");
        for (WorldServer world : MinecraftServer.getServer().worlds) {
            if (world != null) {
                world.loadedEntityList = new ListSet<>(world.loadedEntityList);
                world.unloadedEntityList = new ListSet<>(world.unloadedEntityList);
                world.loadedTileEntityList = new ListSet<>(world.loadedTileEntityList);
                world.addedTileEntityList = new ListSet<>(world.addedTileEntityList);
            }
        }
    }
}
