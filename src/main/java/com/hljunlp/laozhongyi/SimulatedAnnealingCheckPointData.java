package com.hljunlp.laozhongyi;

import java.util.List;
import java.util.Map;

public class SimulatedAnnealingCheckPointData {
    private final float temperature;
    private final float decayRate;
    private final Map<String, String> currentHyperParameters;
    private final float currentScore;
    private final int currentHyperParametersIndex;
    private final List<Float> computedScoresInGroup;
    private final List<Float> currentHyperParameterValues;
    private final Map<String, String> bestHyperParameters;
    private final float bestScore;

    public SimulatedAnnealingCheckPointData(final float temperature, final float decayRate,
                                            final Map<String, String> currentHyperParameters,
                                            final float currentScore,
                                            final int currentHyperParametersIndex,
                                            final List<Float> computedScoresInGroup,
                                            final List<Float> currentHyperParameterValues,
                                            final Map<String, String> bestHyperParameters,
                                            final float bestScore) {
        this.temperature = temperature;
        this.decayRate = decayRate;
        this.currentHyperParameters = currentHyperParameters;
        this.currentScore = currentScore;
        this.currentHyperParametersIndex = currentHyperParametersIndex;
        this.computedScoresInGroup = computedScoresInGroup;
        this.currentHyperParameterValues = currentHyperParameterValues;
        this.bestHyperParameters = bestHyperParameters;
        this.bestScore = bestScore;
    }

    public float getTemperature() {
        return temperature;
    }

    public float getDecayRate() {
        return decayRate;
    }

    public Map<String, String> getCurrentHyperParameters() {
        return currentHyperParameters;
    }

    public float getCurrentScore() {
        return currentScore;
    }

    public int getCurrentHyperParametersIndex() {
        return currentHyperParametersIndex;
    }

    public List<Float> getComputedScoresInGroup() {
        return computedScoresInGroup;
    }

    public List<Float> getCurrentHyperParameterValues() {
        return currentHyperParameterValues;
    }

    public Map<String, String> getBestHyperParameters() {
        return bestHyperParameters;
    }

    public float getBestScore() {
        return bestScore;
    }
}
