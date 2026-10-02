package io.github.crucible;

import java.util.function.BiPredicate;

import net.minecraft.world.World;
import org.bukkit.Location;

/**
 * Lets a plugin stop a natural spawn before the mob is built. WorldGuard sets it for its spawning flags.
 * Building a modded mob only to cancel it in CreatureSpawnEvent costs a lot, and the spawner tries again at once.
 */
public final class NaturalSpawnGate {

    private static volatile BiPredicate<Location, Boolean> test;

    private NaturalSpawnGate() {}

    /** The test gets the spot and whether the mob is a monster, and returns false to stop the spawn. Null removes it. */
    public static void set(BiPredicate<Location, Boolean> gate) {
        test = gate;
    }

    public static boolean allows(World world, int x, int y, int z, boolean monster) {
        BiPredicate<Location, Boolean> gate = test;
        return gate == null || gate.test(new Location(world.getWorld(), x + 0.5D, y, z + 0.5D), monster);
    }
}
