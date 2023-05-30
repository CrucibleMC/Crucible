package io.github.crucible.api;

import com.google.common.annotations.Beta;
import io.github.crucible.nbt.Crucible_JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;

/**
 * Bukkit style collection of useful methods for both mods and plugins
 */
public final class CrucibleAPI {
    private CrucibleAPI() {
        // Seal
    }

    /**
     * Encodes a compound tag to modern SNBT format
     * @param tag nbt tag to encode
     * @return SNBT formatted representation of the tag
     */
    public static String NBTTagToSNBT(NBTTagCompound tag) {
        return tag.crucible_toString();
    }

    /**
     * Parses a modern SNBT back to a compound NBT tag
     * @param tagString SNBT tag to parse
     * @return parsed compound tag
     * @throws NBTException if unable to parse the SNBT
     */
    public static NBTTagCompound NBTTagFromSNBT(String tagString) throws NBTException{
        return Crucible_JsonToNBT.getTagFromJson(tagString);
    }

    // TODO: Actually put the forge bukkit api to work later on

    /**
     * WIP, injects a mod defined plugin into the plugin manager.
     * @param plugin mod plugin.
     */
    @Beta
    public static void registerModPlugin(Plugin plugin) {
        PluginManager pm = Bukkit.getServer().getPluginManager();
        pm.injectModPlugin(plugin);
        pm.enablePlugin(plugin);
    }
}
