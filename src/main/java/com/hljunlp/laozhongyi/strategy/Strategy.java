package com.hljunlp.laozhongyi.strategy;

import java.util.List;

public interface Strategy {
    int chooseSuitableIndex(List<Float> results);

    default void iterationEnd() {
    }

    default boolean ensureIfStop(final boolean shouldStop) {
        return shouldStop;
    }

    default void storeBest() {
    }

    default void restoreBest() {
    }
}
