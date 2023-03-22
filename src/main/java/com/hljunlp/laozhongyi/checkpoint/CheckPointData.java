package com.hljunlp.laozhongyi.checkpoint;

import java.util.HashMap;
import java.util.Map;

public class CheckPointData {
    private final Map<String, String> currentHyperParameters;
    private final int currentHyperParametersIndex;
    private final Map<String, String> bestHyperParameters;
    private final float bestScore;

    private final Map<Map<String, String>, Float> hyperParametersToScore;

    public CheckPointData(final Map<String, String> currentHyperParameters,
                          final int currentHyperParametersIndex, final Map<String,
            String> bestHyperParameters, final float bestScore,
                          Map<Map<String, String>, Float> hyperParametersToScore) {
        this.currentHyperParameters = currentHyperParameters;
        this.currentHyperParametersIndex = currentHyperParametersIndex;
        this.bestHyperParameters = bestHyperParameters;
        this.bestScore = bestScore;
        this.hyperParametersToScore = hyperParametersToScore;
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

    public Map<Map<String, String>, Float> getHyperParametersToScore() {
        return hyperParametersToScore;
    }

    @Override
    public String toString() {
        return "CheckPointData{" +
                "currentHyperParameters=" + currentHyperParameters +
                ", currentHyperParametersIndex=" + currentHyperParametersIndex +
                ", bestHyperParameters=" + bestHyperParameters +
                ", bestScore=" + bestScore +
                ", hyperParametersToScore=" + hyperParametersToScore +
                '}';
    }

    public CheckPointData deepCopy() {
        // Create new instances of the maps
        Map<String, String> newCurrentHyperParameters = new HashMap<>();
        for (Map.Entry<String, String> entry : currentHyperParameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            newCurrentHyperParameters.put(key, value);
        }

        Map<String, String> newBestHyperParameters = new HashMap<>();
        for (Map.Entry<String, String> entry : bestHyperParameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            newBestHyperParameters.put(key, value);
        }

        Map<Map<String, String>, Float> newHyperParametersToScore = new HashMap<>();
        for (Map.Entry<Map<String, String>, Float> entry : hyperParametersToScore.entrySet()) {
            Map<String, String> key = new HashMap<>();
            for (Map.Entry<String, String> innerEntry : entry.getKey().entrySet()) {
                String innerKey = innerEntry.getKey();
                String innerValue = innerEntry.getValue();
                key.put(innerKey, innerValue);
            }
            float value = entry.getValue();
            newHyperParametersToScore.put(key, value);
        }

        // Create a new instance of CheckPointData with the copied data
        return new CheckPointData(newCurrentHyperParameters, currentHyperParametersIndex,
                newBestHyperParameters, bestScore, newHyperParametersToScore);
    }

    public CheckPointData addHyperParameterToScore(final Map<String, String> hyperParameters,
                                                   final float score) {
        CheckPointData newCheckPointData = deepCopy();
        newCheckPointData.hyperParametersToScore.put(hyperParameters, score);
        return newCheckPointData;
    }
}
