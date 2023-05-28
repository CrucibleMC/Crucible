package io.github.crucible.util;

import cpw.mods.fml.common.FMLLog;
import io.github.crucible.nbt.Crucible_JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Hook for plugins to hook
 */
public class ItemUtil {
    public static @Nullable String getItemStackNBTAsString(ItemStack itemStack) {
        if (itemStack instanceof CraftItemStack) { //Dummy items do not have any nbt
            CraftItemStack craftItemStack = (CraftItemStack) itemStack;
            if (craftItemStack.getHandle() != null && craftItemStack.getHandle().hasTagCompound()) {
                return craftItemStack.getHandle().getTagCompound().crucible_toString();
            }
        }
        return null;
    }

    public static ItemStack setItemStackNBTFromString(ItemStack itemStack, String nbt) {
        if (!(itemStack instanceof CraftItemStack)) { //Dummy Items do not have handle field
            itemStack = CraftItemStack.asCraftMirror(CraftItemStack.asNMSCopy(itemStack));
        }
        CraftItemStack craftItemStack = (CraftItemStack) itemStack;
        try {
            NBTTagCompound nbtTagCompound = Crucible_JsonToNBT.getTagFromJson(nbt); //equivalent  MojangsonParser.parse()
            craftItemStack.getHandle().setTagCompound(nbtTagCompound);
            return craftItemStack;
        } catch (NBTException e) {
            FMLLog.info("[Crucible-ItemStackSerializer] Failed to load NBT for " + itemStack + " and NBT[" + nbt + "]");
            e.printStackTrace();
        }
        return null;
    }
}
