package io.github.crucible.bootstrap;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class DownloadProgressBar extends Thread {
    //TODO: figure out other terminal names that supports the necessary escape codes we use
    private static final String[] TERMS = new String[]{
            "xterm",
    };
    private static final boolean FANCY_PROGRESS;

    private final List<? extends ProgressiveObject> objectsToObserve;
    private final AtomicBoolean running = new AtomicBoolean();
    private final AtomicInteger counter = new AtomicInteger();

    public DownloadProgressBar(List<? extends ProgressiveObject> objectsToObserve) {
        this.objectsToObserve = objectsToObserve;
        for (ProgressiveObject progressiveObject : this.objectsToObserve) {
            progressiveObject.offerFinishCounter(counter);
        }
    }

    @Override
    public void run() {
        running.set(true);
        if (FANCY_PROGRESS) {
            int lines = 0;
            int width = 10;
            StringBuilder buffer = new StringBuilder();
            while (running.get()) {
                buffer.setLength(0); // Zeroing an array is faster than allocating a new one
                clearLines(buffer, lines);
                lines = 1; // Already counts the next line we are going to write
                buffer.append(String.format("[Crucible] %s out of %s files downloaded\n", counter.get(), objectsToObserve.size()));

                for (ProgressiveObject task : objectsToObserve) {
                    if (task.isInProgress()) {
                        buffer.append("    [");
                        int chars = (int) ((task.getProgress() / 100) * width);
                        for (int i = 0; i < width; i++) {
                            if (i <= chars) {
                                buffer.append('#');
                            } else {
                                buffer.append(' ');
                            }
                        }
                        buffer.append(String.format("] %.2f%% > %s \n", task.getProgress(), task.displayName()));
                        lines++;
                    }
                }
                System.out.print(buffer);
                Thread.yield();
                try {
                    //noinspection BusyWait
                    Thread.sleep(50);  // Wait some time, we don't need to hammer the console
                } catch (InterruptedException e) {
                    this.interrupt();
                    running.set(false);
                }
            }
            buffer.setLength(0);
            clearLines(buffer, lines);
            System.out.print(buffer);
        } else {
            int lastCount = 0;
            System.out.printf("[Crucible] %s out of %s files downloaded\n", counter.get(), objectsToObserve.size());
            while (running.get()) {
                try {
                    if (counter.get() > lastCount) {
                        lastCount = counter.get();
                        System.out.printf("[Crucible] %s out of %s files downloaded\n", lastCount, objectsToObserve.size());
                    }
                    //noinspection BusyWait
                    Thread.sleep(500);  // Wait some time, we don't need to hammer the console
                } catch (InterruptedException e) {
                    this.interrupt();
                    running.set(false);
                }
            }
        }

    }

    private void clearLines(StringBuilder buffer, int lines) {
        if (lines > 0) {
            buffer.append("\u001b[").append(lines).append("A\r");
            for (int i = 0; i < lines; i++) {
                for (int j = 0; j < 100; j++) {
                    buffer.append(' ');
                }
                buffer.append('\n');
            }
            buffer.append("\u001b[").append(lines).append("A\r");
        }
    }

    public void finish() {
        running.set(false);
    }

    static {
        String terminal = System.getenv("TERM") == null ? "" : System.getenv("TERM");
        // TODO: Finish fancy progress and add proper terminal support
        FANCY_PROGRESS = false; //System.console() != null &&
        //Arrays.stream(TERMS).anyMatch( s -> terminal.toLowerCase(Locale.ROOT).contains(s));
    }
}
