package io.github.crucible.api;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.*;
import io.github.crucible.CrucibleModContainer;
import io.github.crucible.eventfactory.PluginClassLoaderFactory;
import net.minecraftforge.common.MinecraftForge;
import org.bukkit.plugin.Plugin;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CrucibleEventBus {

    /**
     * Register an object to the specific forge event bus!
     *
     *
     * @param plugin The plugin that is registering the event, can be 'null'
     *
     * @param bus The event bus to register to:
     *            - You can use {@link MinecraftForge#EVENT_BUS}
     *            - You can use {@link FMLCommonHandler#bus()}
     *            - Any other bus.
     *
     * @param target Either a {@link Class} or an arbitrary object.
     *               The object maybe have methods annotated with {@link SubscribeEvent}
     *               The class maybe have static methods annotated with {@link SubscribeEvent}
     */
    @SuppressWarnings("unchecked")
    public static void register(Plugin plugin, EventBus bus, Object target) {
        ConcurrentHashMap<Object, ?> listeners = bus.getListeners();
        if (!listeners.containsKey(target)) {
            if (target.getClass() == Class.class) {
                registerClass(plugin, (Class<?>) target, bus);
            } else {
                registerObject(plugin, target, bus);
            }
        }
    }

    private static void registerClass(Plugin plugin, final Class<?> clazz, EventBus bus) {
        Arrays.stream(clazz.getMethods()).
                filter(m -> Modifier.isStatic(m.getModifiers())).
                filter(m -> m.isAnnotationPresent(SubscribeEvent.class)).
                forEach(m -> registerListener(plugin, clazz, m, m, bus));
    }

    private static void registerObject(Plugin plugin, final Object obj, EventBus bus) {
        final HashSet<Class<?>> classes = new HashSet<>();
        typesFor(obj.getClass(), classes);
        Arrays.stream(obj.getClass().getMethods()).
                filter(m -> !Modifier.isStatic(m.getModifiers())).
                forEach(m -> classes.stream().
                        map(c -> getDeclMethod(c, m)).
                        filter(rm -> rm.isPresent() && rm.get().isAnnotationPresent(SubscribeEvent.class)).
                        findFirst().
                        ifPresent(rm -> registerListener(plugin, obj, m, rm.get(), bus)));
    }

    private static void registerListener(Plugin plugin, final Object target, final Method method, final Method real, EventBus bus) {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length != 1) {
            throw new IllegalArgumentException(
                    "Method " + method + " has @SubscribeEvent annotation. " +
                            "It has " + parameterTypes.length + " arguments, " +
                            "but event handler methods require a single argument only."
            );
        }

        Class<?> eventType = parameterTypes[0];

        if (!Event.class.isAssignableFrom(eventType)) {
            throw new IllegalArgumentException(
                    "Method " + method + " has @SubscribeEvent annotation, " +
                            "but takes an argument that is not an Event subtype : " + eventType);
        }

        register(plugin, eventType, target, real, bus);
    }

    private static void register(Plugin plugin, Class<?> eventType, Object target, Method method, EventBus bus) {
        String pluginName = (plugin != null ? plugin.getName() : "null");
        try {
            PluginClassLoaderFactory factory = new PluginClassLoaderFactory();
            IEventListener iEventListener = factory.create(method, target);
            String readable = "ASM_CRUCIBLE: (Plugin='" + pluginName + "') " + target + " " + method.getName() + Type.getMethodDescriptor(method);
            ASMEventHandler asm = new ASMEventHandler(iEventListener, target, method, CrucibleModContainer.instance, readable);
            bus.crucible_register(asm, eventType, target, method, CrucibleModContainer.instance);
        } catch (Throwable e) {
            CrucibleModContainer.logger.error("Error registering event handler for the plugin ({}): {} {}", pluginName, eventType, method, e);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    //  The following methods are utilities that come from isnide the EventBus on modern forge.
    // -----------------------------------------------------------------------------------------------------------------

    private static void typesFor(final Class<?> clz, final Set<Class<?>> visited) {
        if (clz.getSuperclass() == null) return;
        typesFor(clz.getSuperclass(),visited);
        Arrays.stream(clz.getInterfaces()).forEach(i->typesFor(i, visited));
        visited.add(clz);
    }

    private static Optional<Method> getDeclMethod(final Class<?> clz, final Method in) {
        try {
            return Optional.of(clz.getDeclaredMethod(in.getName(), in.getParameterTypes()));
        } catch (NoSuchMethodException nse) {
            return Optional.empty();
        }
    }
}
