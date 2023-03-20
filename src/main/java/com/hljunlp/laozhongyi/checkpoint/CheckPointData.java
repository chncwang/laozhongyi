package com.hljunlp.laozhongyi.checkpoint;

import java.util.Map;

public class CheckPointData {
    private final Map<String, String> currentHyperParameters;
    private final int currentHyperParametersIndex;
    private final Map<String, String> bestHyperParameters;
    private final float bestScore;

    public CheckPointData(final Map<String, String> currentHyperParameters,
                          final int currentHyperParametersIndex, final Map<String,
            String> bestHyperParameters, final float bestScore) {
        this.currentHyperParameters = currentHyperParameters;
        this.currentHyperParametersIndex = currentHyperParametersIndex;
        this.bestHyperParameters = bestHyperParameters;
        this.bestScore = bestScore;
    }

    public Map<String, String> getCurrentHyperParameters() {
        return currentHyperParameters;
    }

    public int getCurrentHyperParametersIndex() {
        return currentHyperParametersIndex;
    }

    public Map<String, String> getBestHyperParameters() {
        return bestHyperParameters;
    }

    public float getBestScore() {
        return bestScore;
    }
}
