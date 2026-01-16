package org.bukkit.craftbukkit.v1_7_R4.util;

import com.mojang.util.QueueLogAppender;
import jline.console.ConsoleReader;
import net.minecraft.server.MinecraftServer;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TerminalConsoleWriterThread implements Runnable {
    final private ConsoleReader reader;
    final private OutputStream output;

    public TerminalConsoleWriterThread(OutputStream output, ConsoleReader reader) {
        this.output = output;
        this.reader = reader;
        // Crucible start - drain the logger since it will have duplicated content
        try {
            Field queues = QueueLogAppender.class.getDeclaredField("QUEUES");
            queues.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, BlockingQueue<String>> instance = (Map<String, BlockingQueue<String>>) queues.get(null);
            instance.get("TerminalConsole").clear();
        } catch (ReflectiveOperationException e) {
            e.printStackTrace();
        }
        // Crucible end
    }

    public void run() {
        String message;

        // Using name from log4j config in vanilla jar
        while (true) {
            message = QueueLogAppender.getNextLogEvent("TerminalConsole");
            if (message == null) {
                Thread.yield();
                continue;
            }

            try {
                if (MinecraftServer.useJline) { // Cauldron
                    reader.print(ConsoleReader.RESET_LINE + "");
                    reader.flush();
                    output.write(message.getBytes());
                    output.flush();

                    try {
                        reader.drawLine();
                    } catch (Throwable ex) {
                        reader.getCursorBuffer().clear();
                    }
                    reader.flush();
                } else {
                    output.write(message.getBytes());
                    output.flush();
                }
            } catch (IOException ex) {
                Logger.getLogger(TerminalConsoleWriterThread.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
