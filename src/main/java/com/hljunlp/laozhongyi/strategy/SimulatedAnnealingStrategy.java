package com.hljunlp.laozhongyi.strategy;

import com.google.common.base.Preconditions;
import com.hljunlp.laozhongyi.checkpoint.SimulatedAnnealingCheckPointData;

import java.util.Random;

public abstract class SimulatedAnnealingStrategy implements Strategy {
    protected float mT;
    protected final float mR;
    protected final Random mRandom;
    protected float mBestT;

    public SimulatedAnnealingStrategy(final float r, final float t) {
        Preconditions.checkArgument(r > 0 && t > 0);
        mT = t;
        mR = r;
        mRandom = new Random();
    }

    public float getTemperature() {
        return mT;
    }

    public float getDecayRate() {
        return mR;
    }

    public SimulatedAnnealingStrategy(final SimulatedAnnealingCheckPointData checkPointData) {
        mT = checkPointData.getTemperature();
        mR = checkPointData.getDecayRate();
        mRandom = new Random();
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
