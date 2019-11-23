package io.github.crucible;


import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

public class CrucibleModContainer extends DummyModContainer {
    public static Logger logger;
    public CrucibleModContainer() {
        super(new ModMetadata());
        ModMetadata meta = getMetadata();
        meta.modId       = "Crucible";
        meta.name        = "Crucible Server";
        meta.version     = "1.0";
        meta.credits     = "TODO: Add credits";
        meta.authorList  = Arrays.asList("juanmuscaria", "brunoxkk0");
        meta.description = "Pure black magic and gambiarras!";
        meta.url         = "https://github.com/CrucibleMC/Crucible";
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
    public void serverStarting(FMLServerStartingEvent evt)
    {
        evt.registerServerCommand("crucible", new CrucibleCommand(evt.getServer()));
    }
    
    @Override
    public List<String> getOwnedPackages()
    {
        return ImmutableList.of(
                "io.github.crucible.entity",
                "io.github.crucible.events",
                "io.github.crucible.hook",
                "io.github.crucible.utils",
                "io.github.crucible.wrapper",
                "io.github.crucible"
                );
    }
}
