package io.github.crucible;


import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.avaje.ebean.EbeanServer;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.WorldAccessContainer;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLLoadCompleteEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.storage.SaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.network.ForgeNetworkHandler;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.oredict.RecipeSorter;
import net.minecraftforge.server.command.ForgeCommand;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.*;

public class CrucibleModContainer extends DummyModContainer implements Plugin {
    public static Logger logger;
    public static CrucibleModContainer instance;
    public static Metrics metrics;
    private PluginLoader dummyPluginLoader;
    private PluginDescriptionFile dummyPluginDescription;
    private boolean isPluginEnabled = false;

    public CrucibleModContainer() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId       = "Crucible";
        meta.name        = "Crucible Server";
        meta.version     = "2.1";
        meta.credits     = "TODO: Add credits";
        meta.authorList  = Arrays.asList("juanmuscaria", "brunoxkk0");
        meta.description = "Pure black magic and gambiarras!";
        meta.url         = "https://github.com/CrucibleMC/Crucible";
        instance = this;
    }
    
    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
        bus.register(this);
        return true;
    }
    
    @Subscribe
    public void modConstruction(FMLConstructionEvent evt)
    {
        NetworkRegistry.INSTANCE.register(this, this.getClass(), "*", evt.getASMHarvestedData());
        System.out.println("[Crucible]-Crucible DummyMod injected successfully!");
    }

    @Subscribe
    public void preInit(FMLPreInitializationEvent evt)
    {
        logger = evt.getModLog();
    }

    @Subscribe
    public void postInit(FMLPostInitializationEvent evt)
    {
    }

    @Subscribe
    public void onAvailable(FMLLoadCompleteEvent evt)
    {
    }

    @Subscribe
    public void serverStarting(FMLServerStartingEvent evt) {
        evt.registerServerCommand("crucible", new CrucibleCommand(evt.getServer()));
        getServer().getPluginManager().loadModPlugin(this);
        getServer().getPluginManager().enablePlugin(this);
        metrics = new Metrics(this, 6555);
    }
    
    @Override
    public List<String> getOwnedPackages()
    {
        return ImmutableList.of(
                "io.github.crucible.remapper",
                "io.github.crucible.entity",
                "io.github.crucible.events",
                "io.github.crucible.hook",
                "io.github.crucible.utils",
                "io.github.crucible.wrapper",
                "io.github.crucible"
                );
    }

    @Override
    public File getDataFolder() {
    return new File(((File) MinecraftServer.options.valueOf("plugins")),"forge");
    }

    @Override
    public PluginDescriptionFile getDescription() {
        if (dummyPluginDescription == null) {
            dummyPluginDescription = new PluginDescriptionFile(CrucibleModContainer.instance.getName(),CrucibleModContainer.instance.getVersion(),CrucibleModContainer.class.getName());
        }
        return dummyPluginDescription;
    }

    @Override
    public FileConfiguration getConfig() {
        return null;
    }

    @Override
    public InputStream getResource(String filename) {
        return null;
    }

    @Override
    public void saveConfig() {

    }

    @Override
    public void saveDefaultConfig() {

    }

    @Override
    public void saveResource(String resourcePath, boolean replace) {

    }

    @Override
    public void reloadConfig() {

    }

    @Override
    public PluginLoader getPluginLoader() {
        if (dummyPluginLoader == null) {
            dummyPluginLoader = new PluginLoader() {
                @Override
                public Plugin loadPlugin(File file) throws InvalidPluginException, UnknownDependencyException {
                    try {
                        return getServer().getPluginManager().loadPlugin(file);
                    } catch (InvalidDescriptionException e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                public PluginDescriptionFile getPluginDescription(File file) {
                    if (dummyPluginDescription == null) {
                    dummyPluginDescription = new PluginDescriptionFile(CrucibleModContainer.instance.getName(),CrucibleModContainer.instance.getVersion(),CrucibleModContainer.class.getName());
                    }
                    return dummyPluginDescription;
                }

                @Override
                public Pattern[] getPluginFileFilters() {
                    return new Pattern[0];
                }

                @Override
                public Map<Class<? extends Event>, Set<RegisteredListener>> createRegisteredListeners(Listener listener, Plugin plugin) {
                    return null;
                }

                @Override
                public void enablePlugin(Plugin plugin) {
                    isPluginEnabled = true;
                }

                @Override
                public void disablePlugin(Plugin plugin) {
                    if (!MinecraftServer.getServer().isServerRunning()) isPluginEnabled = false;
                }
            };
        }
        return dummyPluginLoader;
    }

    @Override
    public Server getServer() {
        return Bukkit.getServer();
    }

    @Override
    public boolean isEnabled() {
        return isPluginEnabled;
    }

    @Override
    public void onDisable() { }

    @Override
    public void onLoad() { }

    @Override
    public void onEnable() { }

    @Override
    public boolean isNaggable() {
        return false;
    }

    @Override
    public void setNaggable(boolean canNag) { }

    @Override
    public EbeanServer getDatabase() {
        return null;
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return null;
    }

    @Override
    public java.util.logging.Logger getLogger() {
        return Bukkit.getLogger();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return null;
    }
}
