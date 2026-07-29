package io.github.crucible.command;

import io.github.crucible.CrucibleConfigs;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.command.Command;

import java.util.List;
import java.util.Locale;

/**
 * Decides whether a command name or alias must be kept out of Bukkit's command map.
 * <p>
 * Dispatching looks the name up in Bukkit's map before falling back to the Forge one, so a Bukkit
 * entry always shadows a mod command sharing that name. Dropping the Bukkit entry is what lets the
 * mod command answer instead.
 */
public final class CommandRegistrationFilter {
    private static final Logger logger = LogManager.getLogger("Crucible");
    private static final String ANY_COMMAND = "*";

    private CommandRegistrationFilter() {
    }

    /**
     * @param fallbackPrefix owner namespace of the registration: {@code bukkit} for Bukkit's own
     *                       commands, {@code minecraft} for the vanilla wrappers, the plugin name
     *                       for plugin commands and the mod id for mod commands.
     * @param label          name or alias being registered, without the leading '/'.
     * @param command        command behind that name; blocking its own name also drops its aliases.
     */
    public static boolean isBlocked(String fallbackPrefix, String label, Command command) {
        List<String> blocked = CrucibleConfigs.configs.crucible_commands_unregister;
        if (blocked.isEmpty()) {
            return false;
        }

        String owner = normalize(fallbackPrefix);
        String name = normalize(label);
        String commandName = command == null ? name : normalize(command.getName());

        for (String entry : blocked) {
            if (matches(normalize(entry), owner, name, commandName)) {
                logger.info("Not registering command '{}:{}' as requested by Crucible.yml", owner, name);
                return true;
            }
        }

        return false;
    }

    private static boolean matches(String entry, String owner, String name, String commandName) {
        int separator = entry.indexOf(':');
        if (separator < 0) {
            return entry.equals(name) || entry.equals(commandName);
        }

        if (!entry.substring(0, separator).equals(owner)) {
            return false;
        }

        String target = entry.substring(separator + 1);
        return target.equals(ANY_COMMAND) || target.equals(name) || target.equals(commandName);
    }

    private static String normalize(String value) {
        if (value == null) {
            return "";
        }

        String normalized = value.trim().toLowerCase(Locale.ROOT);
        return normalized.startsWith("/") ? normalized.substring(1) : normalized;
    }
}
