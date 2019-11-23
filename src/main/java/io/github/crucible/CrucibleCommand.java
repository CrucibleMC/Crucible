package io.github.crucible;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.spigotmc.RestartCommand;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.cauldron.CauldronHooks;
import net.minecraftforge.common.DimensionManager;

public class CrucibleCommand extends Command {

    private static WeakReference<MinecraftServer> serveReference;
    private static final DecimalFormat timeFormat = new DecimalFormat("########0.000");
    
    protected CrucibleCommand(MinecraftServer server){
        super("crucible");
        
        serveReference = new WeakReference<MinecraftServer>(server);
        
        
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
            sender.sendMessage(getTps());
        } else if (args[0].equalsIgnoreCase("restart")) {
            if (!testPermission(sender, "crucible.restart"))
                return true;
            RestartCommand.restart(true);
        } else if (args[0].equalsIgnoreCase("info")) {
            if (!testPermission(sender, "crucible.info"))
                return true;
            sender.sendMessage(generateInfo());
        } else if (args[0].equalsIgnoreCase("chunks")) {
            if (!testPermission(sender, "crucible.chunks"))
                return true;
            processChunks(sender, args);
        } else if (args[0].equalsIgnoreCase("heap")) {
            if (!testPermission(sender, "crucible.heap"))
                return true;
            processHeap(sender, args);
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
        b.append("This server is running &3Crucible&r [v1.0] (Thermos fork by CrucibleMC Team).\n");
        b.append("&9https://github.com/CrucibleMC/Crucible\n&r");
        b.append("Bukkit version: " + Bukkit.getBukkitVersion() + "\n");
        b.append("Minecraft Forge version: " + Bukkit.getVersion() + "\n");
        b.append("Crucible API version: This has not been implemented yet\n");
        b.append("Plugins " + getPluginList() + "\n&r");
        b.append("Mods " + getModList() + "\n&r");
        return b.toString().replace("&", "\u00a7");
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
            pluginList.append(plugin.getDescription().getName() + "@" + plugin.getDescription().getVersion());
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

            modList.append(ChatColor.GREEN + mod.getName() + "@" + mod.getVersion());
        }

        return "(" + mods.size() + "): " + modList.toString();
    }
    
    private static String getTps() {
        StringBuilder tps = new StringBuilder();
        
        tps.append("&8[&e&l\u26a1&r&8] &7TPS from last 1m, 5m, 15m: &r");
        
        double[] recentTps = MinecraftServer.getServer().recentTps;
        
        for(double t : recentTps) {
            tps.append(parseTps(t));
        }
        
        double meanTickTime = mean(getServer().tickTimeArray) * 1.0E-6D;
        tps.append("\n&r&8[&e&l\u26a1&r&8]&7 Mean tick time: &l" + timeFormat.format(meanTickTime) + "&r&7ms&r\n");
        
        for (Integer dimId : DimensionManager.getIDs()){
            
            double worldTickTime = mean(getServer().worldTickTimes.get(dimId)) * 1.0E-6D;
            double worldTPS = Math.min(1000.0/worldTickTime, 20);
            String name = "";
            tps.append("&8(&f&l" + dimId + "&r&f \u279c "+ (((name = DimensionManager.getProvider(dimId).getDimensionName()) != null) ? name : " ") + "&r&8) &7 Mean tick time: &l" + timeFormat.format(worldTickTime) + "&r&7ms Mean tps: " + parseTps(worldTPS)+ "&r\n");
            
        }
        
        tps.append("&r");
        return tps.toString().replace("&", "\u00a7");
    }
    
    private static String parseTps(double tps) {
        
        StringBuilder  t = new StringBuilder();
        
        if(tps <= 10) {
            t.append("&c&l" + String.format("%.2f", Math.min( Math.round( tps * 100.0 ) / 100.0, 20.0 )) + "&r ");
        }else if(tps <= 15) {
            t.append("&e&l" + String.format("%.2f", Math.min( Math.round( tps * 100.0 ) / 100.0, 20.0 )) + "&r ");
        }else {
            t.append("&a&l" + String.format("%.2f", Math.min( Math.round( tps * 100.0 ) / 100.0, 20.0 )) + "&r ");
        }
        
        return  t.toString();
    }
    
    private void processHeap(CommandSender sender, String[] args)
    {
        File file = new File(new File(new File("."), "dumps"), "heap-dump-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-server.bin");
        sender.sendMessage("Writing heap dump to: " + file);
        CauldronHooks.dumpHeap(file, true);
        sender.sendMessage("Heap dump complete.");
    }

    private void processChunks(CommandSender sender, String[] args)
    {
        sender.sendMessage(ChatColor.GOLD + "Dimension stats: ");
        for (net.minecraft.world.WorldServer world : MinecraftServer.getServer().worlds)
        {
            sender.sendMessage(ChatColor.GOLD + "Dimension: " + ChatColor.GRAY + world.provider.dimensionId +
                    ChatColor.GOLD + " Loaded Chunks: " + ChatColor.GRAY + world.theChunkProviderServer.loadedChunkHashMap_KC.size() +
                    ChatColor.GOLD + " Active Chunks: " + ChatColor.GRAY + world.activeChunkSet.size() +
                    ChatColor.GOLD + " Entities: " + ChatColor.GRAY + world.loadedEntityList.size() +
                    ChatColor.GOLD + " Tile Entities: " + ChatColor.GRAY + world.loadedTileEntityList.size()
                    );
            sender.sendMessage(ChatColor.GOLD + " Entities Last Tick: " + ChatColor.GRAY + world.entitiesTicked +
                    ChatColor.GOLD + " Tiles Last Tick: " + ChatColor.GRAY + world.tilesTicked +
                    ChatColor.GOLD + " Removed Entities: " + ChatColor.GRAY + world.unloadedEntityList.size() +
                    ChatColor.GOLD + " Removed Tile Entities: " + ChatColor.GRAY + world.field_147483_b.size()
                    );
        }

        if ((args.length < 2) || !"dump".equalsIgnoreCase(args[1]))
        {
            return;
        }
        boolean dumpAll = ((args.length > 2) && "all".equalsIgnoreCase(args[2]));

        File file = new File(new File(new File("."), "chunk-dumps"), "chunk-info-" + new SimpleDateFormat("yyyy-MM-dd_HH.mm.ss").format(new Date()) + "-server.txt");
        sender.sendMessage("Writing chunk info to: " + file);
        CauldronHooks.writeChunks(file, dumpAll);
        sender.sendMessage("Chunk info complete");
    }
    
    /*
     * Based on ForgeCommand
     */
    private static long mean(long[] values)
    {
        long sum = 0l;
        for (long v : values)
        {
            sum+=v;
        }

        return sum / values.length;
    }
    
    /*
     * Based on ForgeCommand
     */
    private static MinecraftServer getServer()
    {
        return serveReference.get();
    }
    
}
