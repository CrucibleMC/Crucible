package io.github.crucible.api;

import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class CruciblePluginTransform {
    private static final List<IPluginClassTransformer> transformers = new ArrayList<>();

    public static byte[] transform(String name, String transformedName, byte[] basicClass, Plugin plugin) {
        byte[] result = basicClass;
        for(IPluginClassTransformer transformer : transformers) {
            byte[] bytes = transformer.transform(name, transformedName, result, plugin);
            if(bytes != null) {
                result = bytes;
            }
        }
        return result;
    }

    public static void register(IPluginClassTransformer transformer) {
        transformers.add(transformer);
    }

    public static void unregister(IPluginClassTransformer transformer) {
        transformers.remove(transformer);
    }

    @FunctionalInterface
    public interface IPluginClassTransformer {
        byte[] transform(String name, String transformedName, byte[] basicClass, Plugin plugin);
    }
}
