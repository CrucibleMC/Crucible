package io.github.crucible;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.IEventListener;

public class CrucibleTimings {
    public static final Timing forgePreTick = Timings.ofSafe("Forge Pre Server Tick");
    public static final Timing forgePostTick = Timings.ofSafe("Forge Post Server Tick");

    public static Timing getEventTiming(Event event) {
        return Timings.ofSafe("Post Event - " + event.getClass().getName());
    }
    public static Timing getListenerTiming(IEventListener event, Timing timing) {
        return Timings.ofSafe("## Listener - " + event.getClass().getSimpleName(),timing);
    }
}
