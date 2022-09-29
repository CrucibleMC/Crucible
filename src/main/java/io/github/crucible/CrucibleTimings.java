package io.github.crucible;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import com.google.common.collect.MapMaker;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.IEventListener;
import net.minecraft.tileentity.TileEntity;

import java.util.Map;

public class CrucibleTimings {
    public static final Timing forgePreTick = Timings.ofSafe("Forge Pre Server Tick");
    public static final Timing forgePostTick = Timings.ofSafe("Forge Post Server Tick");

    public static Timing getEventTiming(Event event) {
        return Timings.ofSafe("Post Event - " + event.getClass().getName());
    }

    public static Timing getListenerTiming(IEventListener event, Timing timing) {
        return Timings.ofSafe("## Listener - " + getSimpleName(event.getClass()), timing);
    }

    public static Timing getPersonalTimingFromTileEntity(TileEntity tileEntity) {
        //return tileEntity.getPersonalTickTimer(); //TODO: reimplement this, part of EverNife's verbose timing.
        return null;
    }

    private static final Map<Class<?>, String> simpleNameMap = new MapMaker().weakKeys().makeMap(); //Class.getSimpleName() is not cached, we need to create one!

    private static String getSimpleName(Class<?> aClass) {
        return simpleNameMap.computeIfAbsent(aClass, k -> aClass.getSimpleName());
    }
}
