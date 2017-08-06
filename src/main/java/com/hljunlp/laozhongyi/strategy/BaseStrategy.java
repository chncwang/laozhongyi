package com.hljunlp.laozhongyi.strategy;

import java.util.List;

import com.google.common.base.Preconditions;

public class BaseStrategy implements Strategy {
    @Override
    public int chooseSuitableIndex(final List<Float> results, final int originalIndex) {
        float best = -1000;
        int index = -1;
        int i = -1;
        for (final float result : results) {
            ++i;
            if (result > best) {
                best = result;
                index = i;
            }
        }
        Preconditions.checkState(best != -1000);
        return index;
    }
}
