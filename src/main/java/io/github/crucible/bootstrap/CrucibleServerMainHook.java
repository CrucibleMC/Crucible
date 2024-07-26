package io.github.crucible.bootstrap;

import cpw.mods.fml.common.launcher.FMLTweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;

// DO NOT TRY TO LOAD ANY MINECRAFT CLASS FROM HERE, THIS CLASS IS LOADED BEFORE EVERYTHING ON THE SERVER ENTRYPOINT
// Also avoid using streams here
public class CrucibleServerMainHook {
    private static final String[] REPOS = new String[]{
      "https://github.com/juanmuscaria/maven/raw/master/",
      "https://github.com/juanmuscaria/maven/raw/master/ThermosLibs/",
      "https://maven.minecraftforge.net/",
      "https://libraries.minecraft.net/",
      "https://repo.maven.apache.org/maven2/",
      "https://nexus.gtnewhorizons.com/repository/public/"
    };
    private static final Path LIBRARY_ROOT = Paths.get("libraries").toAbsolutePath();
    public static final PrintStream originalOut = System.out;
    public static final PrintStream originalErr = System.err;

    public static void relaunchMain(String[] args) throws Exception {
        System.out.println("[Crucible] Running pre-launch tweaks");

        File injectFile = new File("inject.properties");
        if (!injectFile.exists()) {
            Properties internalProperties = new Properties();
            try {
                internalProperties.load(CrucibleServerMainHook.class.getClassLoader().getResourceAsStream("inject.properties"));
                internalProperties.store(Files.newOutputStream(injectFile.toPath()),
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

        for (Map.Entry<Object, Object> entry : propertiesToInject.entrySet()) {
            System.setProperty((String) entry.getKey(), (String) entry.getValue());
        }

        Lwjgl3ifyGlue.checkJava();

        if(System.getProperty("java.class.loader") == null) {
            System.setProperty("rfb.skipClassLoaderCheck", "true");
        }
        System.setProperty("java.util.logging.manager", "io.github.crucible.JulLogManager");

        if (!verifyLibraries()) {
            setupLibraries();
            System.out.println("[Crucible] Crucible installed! A restart is required to be able to boot.");
            System.exit(0);
        } else {
            System.out.println("[Crucible] Everything in check, booting the server");
        }
    }


    public static void serverMain(String[] args) {
        if (Boolean.parseBoolean(System.getProperty("crucible.i.know.what.i.am.doing.please.crash.the.server")))
            throw new RuntimeException("crucible.i.know.what.i.am.doing.please.crash.the.server=true! If you say so, crashing the server as you asked!");
    }

    private static boolean verifyLibraries() throws IOException, NoSuchAlgorithmException {
        if (Boolean.parseBoolean(System.getProperty("crucible.skipLibraryVerification"))) {
            System.out.println("[Crucible] Skipping library integrity verification");
            return true;
        }
        if (!Files.isDirectory(LIBRARY_ROOT)) {
            return false;
        }
        return LibraryManager.checkIntegrity(LIBRARY_ROOT, CrucibleMetadata.NEEDED_LIBRARIES);
    }

    private static void setupLibraries() throws InterruptedException {
        System.out.println("[Crucible] Setting up server libraries, it may take a few minutes");
        if (Files.exists(LIBRARY_ROOT) && !Files.isDirectory(LIBRARY_ROOT)) {
            throw new IllegalStateException(String.format("Library root '%s' is a file, aborting startup!", LIBRARY_ROOT.toAbsolutePath()));
        }

        String[] userDefinedRepos = System.getProperty("crucible.libraryRepos", "").split(" ");

        List<String> list = new ArrayList<>();
        for (String[] strings : Arrays.asList(REPOS, userDefinedRepos)) {
            for (String s : strings) {
                if (!s.isEmpty()) {
                    list.add(s);
                }
            }
        }
        LibraryManager.downloadMavenLibraries(LIBRARY_ROOT, list.toArray(new String[0]), CrucibleMetadata.NEEDED_LIBRARIES);
    }

    public static void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
}