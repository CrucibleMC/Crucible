package net.minecraftforge.cauldron.inventory;

import net.minecraft.item.crafting.IRecipe;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

/**
 * Bukkit API wrapper for non-vanilla IRecipe classes
 */
public class CustomModRecipe implements Recipe {
    private final IRecipe iRecipe;

    public CustomModRecipe(IRecipe iRecipe) {
        this.iRecipe = iRecipe;
    }

    @Override
    public ItemStack getResult() {
        return CraftItemStack.asCraftMirror(iRecipe.getRecipeOutput());
    }

    public IRecipe getHandle() {
        return iRecipe;
    }
}
