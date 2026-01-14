package io.github.crucible.util;

import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.EnumHelper;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.InventoryType;

import java.util.Map;

public class BukkitEnumHelper {
    private static final Logger logger = LogManager.getLogger();
    // Cauldron start
    public static Biome addBukkitBiome(String name)
    {
        return (Biome) EnumHelper.addEnum(Biome.class, name, new Class[0], new Object[0]);
    }

    public static World.Environment addBukkitEnvironment(int id, String name)
    {
        return (World.Environment) EnumHelper.addEnum(World.Environment.class, name, new Class[] { Integer.TYPE }, new Object[] { Integer.valueOf(id) });
    }

    public static WorldType addBukkitWorldType(String name)
    {
        WorldType worldType = EnumHelper.addEnum(WorldType.class, name, new Class [] { String.class }, new Object[] { name });
        Map<String, WorldType> BY_NAME = ReflectionHelper.getPrivateValue(WorldType.class, null, "BY_NAME");
        BY_NAME.put(name.toUpperCase(), worldType);

        return worldType;
    }

    public static EntityType addBukkitEntityType(String name, Class <? extends org.bukkit.entity.Entity> clazz, int typeId, boolean independent) {
        String entityType = name.replace("-", "_").toUpperCase();
        EntityType bukkitType = EnumHelper.addEnum(EntityType.class, entityType, new Class[] { String.class, Class.class, Integer.TYPE, Boolean.TYPE }, new Object[] { name, clazz, typeId, independent });

        Map<String, EntityType> NAME_MAP = ReflectionHelper.getPrivateValue(EntityType.class, null, "NAME_MAP");
        Map<Short, EntityType> ID_MAP = ReflectionHelper.getPrivateValue(EntityType.class, null, "ID_MAP");

        NAME_MAP.put(name.toLowerCase(), bukkitType);
        ID_MAP.put((short)typeId, bukkitType);


        return bukkitType;
    }

    public static InventoryType addInventoryType(TileEntity tileentity)
    {
        if (!IInventory.class.isAssignableFrom(tileentity.getClass())) return null;
        String id = (String)TileEntity.classToNameMap.get(tileentity.getClass());

        try
        {
            IInventory teInv = (IInventory)tileentity;
            int size = teInv.getSizeInventory();
            return EnumHelper.addEnum(InventoryType.class, id, new Class[]{Integer.TYPE, String.class}, new Object[]{size, id});
        }
        catch (Throwable e)
        {
            if (MinecraftServer.getServer().tileEntityConfig.enableTEInventoryWarning.getValue())
            {
                logger.log(Level.WARN, "Could not create inventory type " + tileentity.getClass().getName() + " Exception: " + e.toString());
                logger.log(Level.WARN, "Could not determine default inventory size for type " + tileentity.getClass().getName() + " using size of 9");
            }
            return EnumHelper.addEnum(InventoryType.class, id, new Class[]{Integer.TYPE, String.class}, new Object[]{9, id});
        }
    }
}
