package io.github.crucible.bootstrap;

import java.util.concurrent.atomic.AtomicInteger;

public interface ProgressiveObject {
    double getProgress();

    boolean isInProgress();

    String displayName();

    void offerFinishCounter(AtomicInteger counter);
}
