package com.hljunlp.laozhongyi.strategy;

import java.util.List;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class VariantSimulatedAnnealingStrategy extends SimulatedAnnealingStrategy {
    public VariantSimulatedAnnealingStrategy(final float r, final float t) {
        super(r, t);
    }

    @Override
    public int chooseSuitableIndex(final List<Float> results, final int originalIndex, final boolean isFirstTry) {
        final List<Double> exps = Lists.newArrayList();
        for (final float result : results) {
            final double exp = (float) Math.exp(result / (mT));
            exps.add(exp);
        }

        double sum = 0;
        for (final double exp : exps) {
            sum += exp;
        }

        for (int i = 0; i < exps.size(); ++i) {
            System.out.println("SimulatedAnnealingStrategy chooseSuitableIndex " + i + " prob:"
                    + exps.get(i) / sum);
        }

        final double rand = mRandom.nextDouble() * sum;

        double sum2 = 0;
        int chosenIndex = -1;
        for (int i = 0; i < exps.size(); ++i) {
            sum2 += exps.get(i);
            if (sum2 >= rand) {
                chosenIndex = i;
                break;
            }
        }
        Preconditions.checkState(chosenIndex != -1);

        return chosenIndex;
    }
}
