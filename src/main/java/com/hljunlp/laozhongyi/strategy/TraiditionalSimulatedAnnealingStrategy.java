package com.hljunlp.laozhongyi.strategy;

import java.util.List;

import com.google.common.base.Preconditions;

public abstract class TraiditionalSimulatedAnnealingStrategy extends SimulatedAnnealingStrategy {
    public TraiditionalSimulatedAnnealingStrategy(final float r, final float t) {
        super(r, t);
    }

    @Override
    public int chooseSuitableIndex(final List<Float> results, final int originalIndex) {
        float max = -1;
        int maxi = -1;
        for (int i = 0; i < results.size(); ++i) {
            if (i == originalIndex) {
                continue;
            }
            if (max < results.get(i)) {
                max = results.get(i);
                maxi = i;
            }
        }

        Preconditions.checkState(maxi != -1);

        System.out.println(
                "TraiditionalSimulatedAnnealingStrategy chooseSuitableIndex: max result is " + max);

        if (max > results.get(originalIndex)) {
            System.out.println(
                    "TraiditionalSimulatedAnnealingStrategy chooseSuitableIndex: original value is "
                            + results.get(originalIndex));
            return maxi;
        } else {
            final float de = max - results.get(originalIndex);
            final float prob = (float) Math.exp(de / mT);
            System.out.println(
                    "TraiditionalSimulatedAnnealingStrategy chooseSuitableIndex prob is " + prob);
            if (mRandom.nextFloat() <= prob) {
                return maxi;
            } else {
                return originalIndex;
            }
        }
    }
}
