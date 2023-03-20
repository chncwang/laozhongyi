package com.hljunlp.laozhongyi.checkpoint;

import java.util.List;
import java.util.Map;

public class SimulatedAnnealingCheckPointData extends CheckPointData {
    private final float temperature;
    private final float decayRate;

    public SimulatedAnnealingCheckPointData(final float temperature, final float decayRate,
                                            final Map<String, String> currentHyperParameters,
                                            final float currentScore,
                                            final int currentHyperParametersIndex,
                                            final List<Float> computedScoresInGroup,
                                            final List<Float> currentHyperParameterValues,
                                            final Map<String, String> bestHyperParameters,
                                            final float bestScore) {
        super(currentHyperParameters, currentScore, currentHyperParametersIndex,
                computedScoresInGroup, currentHyperParameterValues, bestHyperParameters, bestScore);
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
