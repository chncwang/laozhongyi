package com.hljunlp.laozhongyi.checkpoint;

import java.util.Map;

public class SimulatedAnnealingCheckPointData extends CheckPointData {
    private final float temperature;
    private final float decayRate;

    public SimulatedAnnealingCheckPointData(final float temperature, final float decayRate,
                                            final Map<String, String> currentHyperParameters,
                                            final int currentHyperParametersIndex,
                                            final Map<String, String> bestHyperParameters,
                                            final float bestScore,
                                            final Map<Map<String, String>, Float> hyperParametersToScore) {
        super(currentHyperParameters, currentHyperParametersIndex, bestHyperParameters, bestScore
                , hyperParametersToScore);
        this.temperature = temperature;
        this.decayRate = decayRate;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getDecayRate() {
        return decayRate;
    }

    @Override
    public String toString() {
        return "SimulatedAnnealingCheckPointData{" +
                "temperature=" + temperature +
                ", decayRate=" + decayRate +
                "} " + super.toString();
    }

    @Override
    public CheckPointData deepCopy() {
        CheckPointData checkPointData = super.deepCopy();
        return new SimulatedAnnealingCheckPointData(temperature, decayRate,
                checkPointData.getCurrentHyperParameters(),
                checkPointData.getCurrentHyperParametersIndex(),
                checkPointData.getBestHyperParameters(),
                checkPointData.getBestScore(),
                checkPointData.getHyperParametersToScore());
    }
}
