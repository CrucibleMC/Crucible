package io.github.crucible.bootstrap;

import com.gtnewhorizons.retrofuturabootstrap.api.RetroFuturaBootstrap;
import com.gtnewhorizons.retrofuturabootstrap.api.RfbApi;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Attaches the libraries that are downloaded like any other but kept off the jar's
 * {@code Class-Path}, on the JVMs whose Java version can load them.
 *
 * <p>A library too new for the running JVM is not inert on the {@code Class-Path}. Nashorn is the
 * case Crucible ships: it publishes a {@code javax.script.ScriptEngineFactory}, so the first
 * {@link javax.script.ScriptEngineManager} anyone builds makes the JDK instantiate it, and on
 * Java 8 that ends mod loading with {@code UnsupportedClassVersionError} - taking the JDK's own
 * engine down with it, because the failure aborts the whole service scan. The JVM reads the
 * {@code Class-Path} before any Crucible code runs, so the only way to keep such a library away
 * from an old JVM is to never name it there and add it here instead.</p>
 */
public final class DeferredLibraries {

    private static final Path LIBRARY_ROOT = Paths.get("libraries").toAbsolutePath();

    /**
     * Puts every deferred library this JVM is new enough for on the classpath, and leaves the rest
     * on disk untouched for the next JVM that can use them.
     */
    public static void attachSupported() {
        RfbApi rfb = RetroFuturaBootstrap.API;
        for (String entry : CrucibleMetadata.DEFERRED_LIBRARIES) {
            int versionSeparator = entry.lastIndexOf(':');
            if (versionSeparator < 0) {
                System.out.println("[Crucible] Ignoring malformed deferred library '" + entry
                  + "': expected group:artifact:version:minimumJavaMajor.");
                continue;
            }
            String coordinate = entry.substring(0, versionSeparator);
            int minimumJava;
            try {
                minimumJava = Integer.parseInt(entry.substring(versionSeparator + 1));
            } catch (NumberFormatException notAJavaVersion) {
                System.out.println("[Crucible] Ignoring deferred library '" + entry
                  + "': the part after the last colon must be a Java major version, such as 11.");
                continue;
            }
            if (rfb.javaMajorVersion() < minimumJava) {
                continue;
            }
            Path jar = LibraryManager.resolveJar(LIBRARY_ROOT, coordinate);
            if (!Files.isRegularFile(jar)) {
                System.out.println("[Crucible] " + coordinate + " is missing from " + jar
                  + " and will not be available. Start the server without"
                  + " -Dcrucible.skipLibraryVerification to have it downloaded again.");
                continue;
            }
            URL url;
            try {
                url = jar.toUri().toURL();
            } catch (MalformedURLException impossibleForAFilePath) {
                throw new RuntimeException(impossibleForAFilePath);
            }
            // The launch loader answers the ServiceLoader resource scans; the compat loader answers
            // the class loads that a LaunchClassLoader exclusion delegates upwards.
            rfb.launchClassLoader().addURL(url);
            rfb.compatClassLoader().addURL(url);
            System.out.println("[Crucible] Attached " + coordinate + ", supported from Java "
              + minimumJava + " on.");
        }
    }

    private DeferredLibraries() {
    }
}
