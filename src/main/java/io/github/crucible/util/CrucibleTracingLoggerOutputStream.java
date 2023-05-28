package io.github.crucible.util;

import io.github.crucible.CrucibleConfigs;
import org.apache.logging.log4j.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.LoggerOutputStream;
import org.bukkit.craftbukkit.v1_7_R4.command.ColouredConsoleSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoader;
import org.bukkit.plugin.SimplePluginManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.java.PluginClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Frankenstein's monster of {@link cpw.mods.fml.common.TracingPrintStream} and {@link LoggerOutputStream}
 */
public class CrucibleTracingLoggerOutputStream extends ByteArrayOutputStream {

    private static final int BASE_DEPTH = 11;
    private final String separator = System.getProperty("line.separator");
    private final Logger logger;
    private final Level level;
    private final Marker outMaker;

    public CrucibleTracingLoggerOutputStream(Logger logger, Level level) {
        this.logger = logger;
        this.level = level;
        this.outMaker = MarkerManager.getMarker(logger.getName());
    }

    // Note, perhaps this may break when something does not call flush?
    // Print stream should always call flush on new line as long as nobody replace it... again.
    @Override
    public void flush() throws IOException {
        synchronized (this) {
            super.flush();
            String record = this.toString();
            super.reset();

            if ((record.length() > 0) && (!record.equals(separator))) {
                if (CrucibleConfigs.configs.crucible_logging_logStdOutCaller) {
                    LogManager.getLogger(getPrefix()).log(level, outMaker, record);
                } else {
                    logger.log(level, outMaker, record);
                }
            }
            ThreadContext.remove("crucible-className");
            ThreadContext.remove("crucible-methodName");
            ThreadContext.remove("crucible-lineNumer");
        }
    }
    

    private String getPrefix() {
        StackTraceElement[] elems = Thread.currentThread().getStackTrace();
        if (elems.length == 0) {
            // Unsupported environment, eh better prevent a crash then?
            return logger.getName();
        }
        StackTraceElement elem = elems[BASE_DEPTH]; // The caller is always at BASE_DEPTH, including this call.
        if (elem.getClassName().startsWith("kotlin.io.")) {
            elem = elems[BASE_DEPTH + 2]; // Kotlins IoPackage masks origins 2 deeper in the stack.
        } else if (elem.getClassName().equals(ColouredConsoleSender.class.getCanonicalName())) {
            elem = elems[BASE_DEPTH+1]; // Special case for bukkit plugins using colored console sender
            String plugin = figureOutPluginPrefix(elem.getClassName());
            if (plugin != null) {
                ThreadContext.put("crucible-className", elem.getClassName());
                ThreadContext.put("crucible-methodName", elem.getMethodName());
                ThreadContext.put("crucible-lineNumer", Integer.toString(elem.getLineNumber()));
                return plugin;
            }
            // Eh, we can't figure out a plugin calling the console sender, print the class instead
        }
        ThreadContext.put("crucible-className", elem.getClassName());
        ThreadContext.put("crucible-methodName", elem.getMethodName());
        ThreadContext.put("crucible-lineNumer", Integer.toString(elem.getLineNumber()));
        return elem.getClassName();
    }

    // This solution looks way too slow, possibly change this in the future?
    private String figureOutPluginPrefix(String clazzName) {
        for (PluginLoader loader : ((SimplePluginManager) Bukkit.getPluginManager()).getLoaders()) {
            if (loader instanceof JavaPluginLoader) {
                JavaPluginLoader jLoader = (JavaPluginLoader) loader;
                Class<?> clazz = jLoader.getClassByName(clazzName);
                if (clazz != null && clazz.getClassLoader() instanceof PluginClassLoader) {
                    Plugin plugin = ((PluginClassLoader) clazz.getClassLoader()).getPlugin();
                    String prefix = plugin.getDescription().getPrefix();
                    return prefix != null ? prefix : plugin.getDescription().getName();
                }
            }
        }
        return null;
    }
}