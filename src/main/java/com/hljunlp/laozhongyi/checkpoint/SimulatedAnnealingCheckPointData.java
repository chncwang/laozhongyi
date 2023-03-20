package com.hljunlp.laozhongyi.checkpoint;

import java.util.Map;

public class SimulatedAnnealingCheckPointData extends CheckPointData {
    private final float temperature;
    private final float decayRate;

    public SimulatedAnnealingCheckPointData(final float temperature, final float decayRate,
                                            final Map<String, String> currentHyperParameters,
                                            final int currentHyperParametersIndex,
                                            final Map<String, String> bestHyperParameters,
                                            final float bestScore) {
        super(currentHyperParameters, currentHyperParametersIndex, bestHyperParameters, bestScore);
        this.temperature = temperature;
        this.decayRate = decayRate;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getDecayRate() {
        return decayRate;
    }
}
