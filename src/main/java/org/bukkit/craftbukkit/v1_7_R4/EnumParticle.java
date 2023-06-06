package org.bukkit.craftbukkit.v1_7_R4;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Map;

public enum EnumParticle {

    EXPLOSION_NORMAL("explode", 0, true),
    EXPLOSION_LARGE("largeexplode", 1, true),
    EXPLOSION_HUGE("hugeexplosion", 2, true),
    FIREWORKS_SPARK("fireworksSpark", 3, false),
    WATER_BUBBLE("bubble", 4, false),
    WATER_SPLASH("splash", 5, false),
    WATER_WAKE("wake", 6, false),
    SUSPENDED("suspended", 7, false),
    SUSPENDED_DEPTH("depthsuspend", 8, false),
    CRIT("crit", 9, false),
    CRIT_MAGIC("magicCrit", 10, false),
    SMOKE_NORMAL("smoke", 11, false),
    SMOKE_LARGE("largesmoke", 12, false),
    SPELL("spell", 13, false),
    SPELL_INSTANT("instantSpell", 14, false),
    SPELL_MOB("mobSpell", 15, false),
    SPELL_MOB_AMBIENT("mobSpellAmbient", 16, false),
    SPELL_WITCH("witchMagic", 17, false),
    DRIP_WATER("dripWater", 18, false),
    DRIP_LAVA("dripLava", 19, false),
    VILLAGER_ANGRY("angryVillager", 20, false),
    VILLAGER_HAPPY("happyVillager", 21, false),
    TOWN_AURA("townaura", 22, false),
    NOTE("note", 23, false),
    PORTAL("portal", 24, false),
    ENCHANTMENT_TABLE("enchantmenttable", 25, false),
    FLAME("flame", 26, false),
    LAVA("lava", 27, false),
    FOOTSTEP("footstep", 28, false),
    CLOUD("cloud", 29, false),
    REDSTONE("reddust", 30, false),
    SNOWBALL("snowballpoof", 31, false),
    SNOW_SHOVEL("snowshovel", 32, false),
    SLIME("slime", 33, false),
    HEART("heart", 34, false),
    BARRIER("barrier", 35, false),
    ITEM_CRACK("iconcrack_", 36, false, 2),
    BLOCK_CRACK("blockcrack_", 37, false, 1),
    BLOCK_DUST("blockdust_", 38, false, 1),
    WATER_DROP("droplet", 39, false),
    ITEM_TAKE("take", 40, false),
    MOB_APPEARANCE("mobappearance", 41, true);

    private final String Q;
    private final int R;
    private final boolean S;
    private final int T;
    private static final Map<Integer, EnumParticle> U = Maps.newHashMap();
    private static final String[] V;

    EnumParticle(String s, int i, boolean flag, int j) {
        this.Q = s;
        this.R = i;
        this.S = flag;
        this.T = j;
    }

    EnumParticle(String s, int i, boolean flag) {
        this(s, i, flag, 0);
    }

    public static String[] a() {
        return EnumParticle.V;
    }

    public String b() {
        return this.Q;
    }

    public int c() {
        return this.R;
    }

    public int d() {
        return this.T;
    }

    public boolean e() {
        return this.S;
    }

    public boolean f() {
        return this.T > 0;
    }

    public static EnumParticle a(int i) {
        return EnumParticle.U.get(i);
    }

    static {
        ArrayList arraylist = Lists.newArrayList();
        EnumParticle[] aenumparticle = values();
        int i = aenumparticle.length;

        for (EnumParticle enumparticle : aenumparticle) {
            EnumParticle.U.put(enumparticle.c(), enumparticle);
            if (!enumparticle.b().endsWith("_")) {
                arraylist.add(enumparticle.b());
            }
        }

        V = (String[]) arraylist.toArray(new String[arraylist.size()]);
    }
}