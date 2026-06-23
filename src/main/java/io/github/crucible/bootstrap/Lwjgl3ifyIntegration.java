package io.github.crucible.bootstrap;

import io.github.crucible.CrucibleConfigs;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

/**
 * Integration with lwjgl3ify mod when present as optional dependency.
 * This class uses reflection to avoid hard dependency on lwjgl3ify.
 * 
 * IMPORTANT: This must be called BEFORE any enum classes are loaded, as the
 * ExtensibleEnumTransformer checks EarlyConfig.EXTENSIBLE_ENUMS during class transformation.
 */
public class Lwjgl3ifyIntegration {
    
    private static boolean initialized = false;
    private static boolean lwjgl3ifyPresent = false;
    
    /**
     * Attempts to register extensible enums with lwjgl3ify if it's present.
     * This method MUST be called early in the initialization process, before any enum classes are loaded.
     * 
     * This directly adds enums to EarlyConfig.EXTENSIBLE_ENUMS set before the transformer runs.
     */
    public static void registerExtensibleEnums() {
        if (initialized) {
            return;
        }
        initialized = true;
        
        try {
            // Try to load lwjgl3ify's EarlyConfig class
            // Note: EarlyConfig is excluded from LaunchClassLoader, so it loads in the system classloader
            Class<?> earlyConfigClass = Class.forName("me.eigenraven.lwjgl3ify.rfb.EarlyConfig");
            lwjgl3ifyPresent = true;
            
            System.out.println("[Crucible] lwjgl3ify detected, registering extensible enums...");
            
            // Get the list of extensible enums from Crucible config
            List<String> extensibleEnums = CrucibleConfigs.configs.lwjgl3ify_extensibleEnums;
            
            if (extensibleEnums == null || extensibleEnums.isEmpty()) {
                System.out.println("[Crucible] No extensible enums configured, skipping registration.");
                return;
            }
            
            // Get the EXTENSIBLE_ENUMS field directly
            Field extensibleEnumsField = earlyConfigClass.getDeclaredField("EXTENSIBLE_ENUMS");
            extensibleEnumsField.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            Set<String> lwjgl3ifyEnums = (Set<String>) extensibleEnumsField.get(null);
            
            // Add all Crucible enums to lwjgl3ify's set
            int registered = 0;
            for (String enumClass : extensibleEnums) {
                if (lwjgl3ifyEnums.add(enumClass)) {
                    registered++;
                }
            }
            
            System.out.println("[Crucible] Successfully registered " + registered + " extensible enums with lwjgl3ify.");
            System.out.println("[Crucible] Total extensible enums: " + lwjgl3ifyEnums.size());
            
        } catch (ClassNotFoundException e) {
            // lwjgl3ify is not present, this is fine
            System.out.println("[Crucible] lwjgl3ify not detected, extensible enum registration skipped.");
        } catch (Exception e) {
            System.err.println("[Crucible] Error while registering extensible enums with lwjgl3ify:");
            e.printStackTrace();
        }
    }
    
    /**
     * @return true if lwjgl3ify is present and initialized
     */
    public static boolean isLwjgl3ifyPresent() {
        return lwjgl3ifyPresent;
    }
}
