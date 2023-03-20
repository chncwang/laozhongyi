package com.hljunlp.laozhongyi.checkpoint;

import java.util.List;
import java.util.Map;

public class CheckPointData {
    private final Map<String, String> currentHyperParameters;
    private final float currentScore;
    private final int currentHyperParametersIndex;
    private final List<Float> currentHyperParameterValues;
    private final Map<String, String> bestHyperParameters;
    private final float bestScore;

    public CheckPointData(final Map<String, String> currentHyperParameters,
                          final float currentScore, final int currentHyperParametersIndex,
                          final List<Float> currentHyperParameterValues, final Map<String,
            String> bestHyperParameters, final float bestScore) {
        this.currentHyperParameters = currentHyperParameters;
        this.currentScore = currentScore;
        this.currentHyperParametersIndex = currentHyperParametersIndex;
        this.currentHyperParameterValues = currentHyperParameterValues;
        this.bestHyperParameters = bestHyperParameters;
        this.bestScore = bestScore;
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
