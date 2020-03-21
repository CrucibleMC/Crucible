package io.github.crucible;

import co.aikar.timings.Timing;
import co.aikar.timings.Timings;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.IEventListener;

import java.util.HashMap;

public class CrucibleTimings {
    public static final Timing forgePreTick = Timings.ofSafe("Forge Pre Server Tick");
    public static final Timing forgePostTick = Timings.ofSafe("Forge Post Server Tick");

    public static Timing getEventTiming(Event event) {
        return Timings.ofSafe("Post Event - " + event.getClass().getName());
    }
    public static Timing getListenerTiming(IEventListener event, Timing timing) {
        return Timings.ofSafe("## Listener - " + getSimpleName(event.getClass()),timing);
    }

    private static HashMap<Class,String> simpleNameMap = new HashMap<>(); //Class.getSimpleName() is not cached, we need to create one!
    private static String getSimpleName(Class aClass){
        String simpleName = simpleNameMap.get(aClass);
        if (simpleName == null) {
            simpleName = aClass.getSimpleName();
            simpleNameMap.put(aClass, simpleName);
        }
        return simpleName;
    }
}
