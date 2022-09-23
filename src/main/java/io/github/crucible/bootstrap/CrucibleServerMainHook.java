package io.github.crucible.bootstrap;

import io.github.crucible.CrucibleMetadata;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;
import java.util.stream.Stream;

// DO NOT TRY TO LOAD ANY MINECRAFT CLASS FROM HERE, THIS CLASS IS LOADED BEFORE EVERYTHING ON THE SERVER ENTRYPOINT
public class CrucibleServerMainHook {
    private static final String[] REPOS = new String[] {
            "https://github.com/juanmuscaria/maven/raw/master/",
            "https://github.com/juanmuscaria/maven/raw/master/ThermosLibs/",
            "https://maven.minecraftforge.net/",
            "https://libraries.minecraft.net/",
            "https://repo.maven.apache.org/maven2/"
    };
    private static final Path LIBRARY_ROOT = Paths.get("libraries").toAbsolutePath();

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
        propertiesToInject.forEach((key, value) -> System.setProperty((String) key, (String) value));

        if (!verifyLibraries()) {
            setupLibraries();
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

        LibraryManager.downloadMavenLibraries(LIBRARY_ROOT, Stream.of(REPOS, userDefinedRepos).flatMap(Stream::of)
                .filter(s -> !s.isEmpty()).toArray(String[]::new), CrucibleMetadata.NEEDED_LIBRARIES);
    }
}