package io.github.crucible.bootstrap;

import net.minecraft.launchwrapper.LaunchClassLoader;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

public class Lwjgl3ifyGlue {
    public static final String[] DEFAULT_EXTENSIBLE_ENUMS = new String[] {
        // From EnumHelper
        "net.minecraft.item.EnumAction", "net.minecraft.item.ItemArmor$ArmorMaterial",
        "net.minecraft.entity.item.EntityPainting$EnumArt", "net.minecraft.entity.EnumCreatureAttribute",
        "net.minecraft.entity.EnumCreatureType",
        "net.minecraft.world.gen.structure.StructureStrongholdPieces$Stronghold$Door",
        "net.minecraft.enchantment.EnumEnchantmentType", "net.minecraft.entity.Entity$EnumEntitySize",
        "net.minecraft.block.BlockPressurePlate$Sensitivity",
        "net.minecraft.util.MovingObjectPosition$MovingObjectType", "net.minecraft.world.EnumSkyBlock",
        "net.minecraft.entity.player.EntityPlayer$EnumStatus", "net.minecraft.item.Item$ToolMaterial",
        "net.minecraft.item.EnumRarity",
        //
        "net.minecraftforge.event.terraingen.PopulateChunkEvent$Populate$EventType",
        "net.minecraftforge.event.terraingen.InitMapGenEvent$EventType",
        "net.minecraftforge.event.terraingen.OreGenEvent$GenerateMinable$EventType",
        "net.minecraftforge.event.terraingen.DecorateBiomeEvent$Decorate$EventType",
        // From GTNH crashes
        "vswe.stevesfactory.Localization", "vswe.stevesfactory.blocks.ClusterMethodRegistration",
        "vswe.stevesfactory.blocks.ConnectionBlockType", "vswe.stevesfactory.components.ComponentType",
        "vswe.stevesfactory.components.ConnectionSet", "vswe.stevesfactory.components.ConnectionOption",
        "ic2.core.init.InternalName", "gregtech.api.enums.Element", "gregtech.api.enums.OrePrefixes",
        "net.minecraft.client.audio.MusicTicker$MusicType",
        "buildcraft.api.transport.IPipeTile.PipeType", "thaumcraft.common.entities.golems.EnumGolemType",
        // Non-GTNH Mods Compat
        // The Lord of the Rings Mod: Legacy
        "net.minecraft.event.HoverEvent$Action",
        // Reika's mods
        "net.minecraft.client.audio.SoundCategory",
        "Reika.RotaryCraft.TileEntities.Processing.TileEntityFuelConverter$Conversions",
        "Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry$Fertility",
        "Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry$Speeds",
        "Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry$Flowering",
        "Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry$Territory",
        "Reika.DragonAPI.ModInteract.Bees.BeeAlleleRegistry$Life",
        "Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry$Fertility",
        "Reika.DragonAPI.ModInteract.Bees.ButterflyAlleleRegistry$Life",
        // Et Futurum Requiem
        "net.minecraft.world.WorldSettings$GameType",
        // Minechem
        "net.minecraftforge.common.BiomeDictionary$Type",
        // Bukkit Enums
        "org.bukkit.Material",
        "org.bukkit.block.Biome",
        "org.bukkit.World$Environment",
        "org.bukkit.WorldType",
        "org.bukkit.entity.EntityType",
        "org.bukkit.event.inventory.InventoryType",
    };

    public static void checkJava() {
        final String specVer = System.getProperty("java.specification.version");

        // Is there any jvm where the specs is 8 instead of 1.8?
        if (!Boolean.getBoolean("crucible.weAreJava9") && !(specVer.equals("1.8"))) {
            System.out.println("[Crucible] Looks like you are missing the special java9+, the server may not launch without them.");
        }

        if (specVer.equals("1.8")) {
            System.out.println("[Crucible] Crucible now supports Java 8-21 by embedding a modified version of lwjgl3ify (https://github.com/GTNewHorizons/lwjgl3ify/).");
        } else {
            System.out.println("[Crucible] Crucible is running modified version of lwjgl3ify (https://github.com/GTNewHorizons/lwjgl3ify/).");
        }
        System.out.println("[Crucible] Do not report issues to upstream. All issues with newer Java version must be reported to Crucible's issue tracker instead.");

        if (!Boolean.getBoolean("lwjgl3ify.skipjavacheck")) {
            if (specVer.equals("17")) {
                try {
                    final Class<?> cRuntime = Class.forName("java.lang.Runtime");
                    final Class<?> cRuntimeVersion = Class.forName("java.lang.Runtime$Version");
                    final MethodHandles.Lookup lookup = MethodHandles.publicLookup();
                    final MethodHandle mRuntimeVersion = lookup
                        .findStatic(cRuntime, "version", MethodType.methodType(cRuntimeVersion));
                    final MethodHandle mVersionParse = lookup
                        .findStatic(cRuntimeVersion, "parse", MethodType.methodType(cRuntimeVersion, String.class));
                    final MethodHandle mVersionCompareTo = lookup.findVirtual(
                        cRuntimeVersion,
                        "compareToIgnoreOptional",
                        MethodType.methodType(int.class, cRuntimeVersion));
                    final Object runtimeVersion = mRuntimeVersion.invoke();
                    final Object ver17_0_6 = mVersionParse.invoke("17.0.6");
                    final int cmpResult = (int) mVersionCompareTo.invoke(runtimeVersion, ver17_0_6);
                    if (cmpResult < 0) {
                        System.err.println("=================================================================");
                        System.err.println("Upgrade your Java 17 install to Java 17.0.6 for lwjgl3ify to work");
                        System.err.println("Your Java version is: " + runtimeVersion);
                        System.err.println("=================================================================");
                        System.exit(1);
                    }
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void doCoremodWork(LaunchClassLoader launchLoader) {
        launchLoader.addClassLoaderExclusion("com.sun");
        launchLoader.addClassLoaderExclusion("com.oracle");
        launchLoader.addClassLoaderExclusion("javax");
        launchLoader.addClassLoaderExclusion("jdk");
        launchLoader.addClassLoaderExclusion("org.ietf.jgss");
        launchLoader.addClassLoaderExclusion("org.jcp.xml.dsig.internal");
        launchLoader.addClassLoaderExclusion("org.omg");
        launchLoader.addClassLoaderExclusion("org.w3c.dom");
        launchLoader.addClassLoaderExclusion("org.xml.sax");
        launchLoader.addClassLoaderExclusion("org.hotswap.agent");
        launchLoader.addClassLoaderExclusion("org.lwjglx.debug");
        try {
            Class.forName("javax.script.ScriptEngineManager");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        launchLoader.registerTransformer("me.eigenraven.lwjgl3ify.core.LwjglRedirectTransformer");
        launchLoader.registerTransformer("me.eigenraven.lwjgl3ify.core.UnfinalizeObjectHoldersTransformer");
    }
}
