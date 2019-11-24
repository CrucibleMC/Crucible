package io.github.crucible;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

public class CrucibleTickHandler {
    
    public static boolean canTickTile(TileEntity tile) {
        return true;
    }
    
    public static boolean canTickEntity(Entity entity) {
        return true;
    }

}
