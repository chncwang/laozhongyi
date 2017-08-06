package com.hljunlp.laozhongyi.strategy;

import java.util.List;
import java.util.Random;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class SimulatedAnnealingStrategy implements Strategy {
    private float mT;
    private final float mR;
    private final Random mRandom;
    private float mBestT;

    public SimulatedAnnealingStrategy(final float r, final float t) {
        Preconditions.checkArgument(r > 0 && t > 0);
        mT = t;
        mR = r;
        mRandom = new Random();
    }

    @Override
    public int chooseSuitableIndex(final List<Float> results) {
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

    @Override
    public void iterationEnd() {
        final float next = mT * mR;
        mT = next >= 0.001 ? next : mT;
        System.out.println("SimulatedAnnealingStrategy iterationEnd mT:" + mT);
    }

    @Override
    public boolean ensureIfStop(final boolean shouldStop) {
        return shouldStop && mT * mR < 0.001;
    }

    @Override
    public void storeBest() {
        mBestT = mT;
    }

    @Override
    public void restoreBest() {
        mT = mBestT;
    }
}
