package io.github.crucible;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.spigotmc.RestartCommand;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public class CrucibleCommand extends Command{

    protected CrucibleCommand()
    {
        super("crucible");
        StringBuilder b = new StringBuilder();
        b.append("-------------------[" + ChatColor.BLUE + "Crucible" + ChatColor.RESET + "]-------------------\n");
        b.append("/crucible tps - Show tps statistics.\n");
        b.append("/crucible restart - Restart the server.\n");
        b.append("/crucible info - Print some informations about the server.\n");
        setUsage(b.toString());
        setPermission("crucible");
   
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args)
    {
        if (!testPermission(sender))
            return true;
        if (args.length == 0) {
            sender.sendMessage(ChatColor.BLUE + "[Crucible] " + ChatColor.GRAY + "Please specify action");
            sender.sendMessage(usageMessage);
            return true;
        }
        if (args[0].equalsIgnoreCase("tps")) {
            if (!testPermission(sender, "crucible.tps"))
                return true;
            sender.sendMessage(ChatColor.RED + "This has not been implemented yet.");
        } else if (args[0].equalsIgnoreCase("restart")) {
            if (!testPermission(sender, "crucible.restart"))
                return true;
            RestartCommand.restart(true);
        } else if (args[0].equalsIgnoreCase("info")) {
            if (!testPermission(sender, "crucible.info"))
                return true;
            sender.sendMessage(generateInfo());
        } else {
            sender.sendMessage(ChatColor.RED + "Unknown subcommand.");
            sender.sendMessage(usageMessage);
        }
        return true;
    }

    public boolean testPermission(CommandSender target, String permission) {
        if (testPermissionSilent(target, permission)) {
            return true;
        }
        target.sendMessage(ChatColor.BLUE + "[Crucible] " + ChatColor.DARK_RED + "I'm sorry, but you do not have permission to perform this command. Please contact the server administrators if you believe that this is an error.");
        return false;
    }
    
    public boolean testPermissionSilent(CommandSender target, String permission) {
        if (!super.testPermissionSilent(target)) {
            return false;
        }
        for (String p : permission.split(";"))
            if (target.hasPermission(p))
                return true;
        return false;
    }
    
    public static String generateInfo() {
        StringBuilder b = new StringBuilder();
        b.append("This server is running §3Crucible§r [v1.0] (Thermos fork by CrucibleMC Team).\n");
        b.append("§9https://github.com/CrucibleMC/Crucible\n§r");
        b.append("Bukkit version: " + Bukkit.getBukkitVersion() + "\n");
        b.append("Minecraft Forge version: " + Bukkit.getVersion() + "\n");
        b.append("Crucible API version: This has not been implemented yet\n");
        b.append("Plugins " + getPluginList() + "\n§r");
        b.append("Mods " + getModList() + "\n§r");
        return b.toString();
    }
    
    private static String getPluginList() {
        StringBuilder pluginList = new StringBuilder();
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();

        for (Plugin plugin : plugins) {
            if (pluginList.length() > 0) {
                pluginList.append(ChatColor.WHITE);
                pluginList.append(", ");
            }

            pluginList.append(plugin.isEnabled() ? ChatColor.GREEN : ChatColor.RED);
            pluginList.append(plugin.getDescription().getName());
        }

        return "(" + plugins.length + "): " + pluginList.toString();
    }
    
    private static String getModList() {
        StringBuilder modList = new StringBuilder();
        
        List<ModContainer> mods = Loader.instance().getActiveModList();
        
        for (ModContainer mod : mods) {
            if (modList.length() > 0) {
                modList.append(ChatColor.WHITE);
                modList.append(", ");
            }

            modList.append(ChatColor.GREEN + mod.getName());
        }

        return "(" + mods.size() + "): " + modList.toString();
    }
}
