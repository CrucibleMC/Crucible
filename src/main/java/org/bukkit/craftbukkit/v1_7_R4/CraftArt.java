package org.bukkit.craftbukkit.v1_7_R4;

import org.bukkit.Art;

// Safety class, will break if either side changes
public class CraftArt {

    public static Art NotchToBukkit(net.minecraft.entity.item.EntityPainting.EnumArt art) {
        switch (art) {
            case Kebab:
                return Art.KEBAB;
            case Aztec:
                return Art.AZTEC;
            case Alban:
                return Art.ALBAN;
            case Aztec2:
                return Art.AZTEC2;
            case Bomb:
                return Art.BOMB;
            case Plant:
                return Art.PLANT;
            case Wasteland:
                return Art.WASTELAND;
            case Pool:
                return Art.POOL;
            case Courbet:
                return Art.COURBET;
            case Sea:
                return Art.SEA;
            case Sunset:
                return Art.SUNSET;
            case Creebet:
                return Art.CREEBET;
            case Wanderer:
                return Art.WANDERER;
            case Graham:
                return Art.GRAHAM;
            case Match:
                return Art.MATCH;
            case Bust:
                return Art.BUST;
            case Stage:
                return Art.STAGE;
            case Void:
                return Art.VOID;
            case SkullAndRoses:
                return Art.SKULL_AND_ROSES;
            case Fighters:
                return Art.FIGHTERS;
            case Pointer:
                return Art.POINTER;
            case Pigscene:
                return Art.PIGSCENE;
            case BurningSkull:
                return Art.BURNINGSKULL;
            case Skeleton:
                return Art.SKELETON;
            case DonkeyKong:
                return Art.DONKEYKONG;
            case Wither:
                return Art.WITHER;
            default:
                throw new AssertionError(art);
        }
    }

    public static net.minecraft.entity.item.EntityPainting.EnumArt BukkitToNotch(Art art) {
        switch (art) {
            case KEBAB:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Kebab;
            case AZTEC:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Aztec;
            case ALBAN:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Alban;
            case AZTEC2:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Aztec2;
            case BOMB:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Bomb;
            case PLANT:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Plant;
            case WASTELAND:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Wasteland;
            case POOL:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Pool;
            case COURBET:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Courbet;
            case SEA:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Sea;
            case SUNSET:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Sunset;
            case CREEBET:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Creebet;
            case WANDERER:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Wanderer;
            case GRAHAM:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Graham;
            case MATCH:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Match;
            case BUST:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Bust;
            case STAGE:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Stage;
            case VOID:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Void;
            case SKULL_AND_ROSES:
                return net.minecraft.entity.item.EntityPainting.EnumArt.SkullAndRoses;
            case FIGHTERS:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Fighters;
            case POINTER:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Pointer;
            case PIGSCENE:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Pigscene;
            case BURNINGSKULL:
                return net.minecraft.entity.item.EntityPainting.EnumArt.BurningSkull;
            case SKELETON:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Skeleton;
            case DONKEYKONG:
                return net.minecraft.entity.item.EntityPainting.EnumArt.DonkeyKong;
            case WITHER:
                return net.minecraft.entity.item.EntityPainting.EnumArt.Wither;
            default:
                throw new AssertionError(art);
        }
    }
}
