package io.github.crucible;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

// DO NOT TRY TO LOAD ANY MINECRAFT CLASS FROM HERE, THIS CLASS IS LOADED BEFORE EVERYTHING ON THE SERVER ENTRYPOINT
public class CrucibleServerMainHook {
    public static void relaunchMain(String[] args) {
        File injectFile = new File("inject.properties");
        if (!injectFile.exists()) {
            Properties internalProperties = new Properties();
            try {
                internalProperties.load(CrucibleServerMainHook.class.getClassLoader().getResourceAsStream("inject.properties"));
                internalProperties.store(new FileOutputStream(injectFile),
                        "All properties in this file will be injected into System Properties before the server starts. Useful for shared hostings");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Properties propertiesToInject = new Properties();
        try {
            propertiesToInject.load(new FileReader(injectFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        propertiesToInject.forEach((key, value) -> System.setProperty((String) key, (String) value));
    }

    public static void serverMain(String[] args) {
        if (Boolean.parseBoolean(System.getProperty("crucible.crashme")))
            throw new RuntimeException("DEBUG CRASH! crucible.crashme=true!!!!!!");
    }
}