package io.github.crucible.command;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

import java.util.Locale;

/**
 * Namespace a Forge command is filed under on the Bukkit side, so that mod commands are addressable
 * as {@code modid:command} and can be told apart from the Bukkit and vanilla ones.
 */
public final class ModCommandNamespace {
    private static final String VANILLA = "minecraft";

    private ModCommandNamespace() {
    }

    /**
     * Mod id of whoever is registering right now, or {@code minecraft} when no mod is being served
     * an event, which is the case for the vanilla commands the server registers itself.
     */
    public static String current() {
        ModContainer container = Loader.instance().activeModContainer();
        if (container == null) {
            return VANILLA;
        }

        String modId = container.getModId();
        return modId == null || modId.isEmpty() ? VANILLA : modId.toLowerCase(Locale.ROOT);
    }
}
