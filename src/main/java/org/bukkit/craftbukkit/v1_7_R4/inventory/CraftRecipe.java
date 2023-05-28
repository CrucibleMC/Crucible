package org.bukkit.craftbukkit.v1_7_R4.inventory;

import org.bukkit.inventory.Recipe;

public interface CraftRecipe extends Recipe {
    void addToCraftingManager();
}
