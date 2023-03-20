package com.hljunlp.laozhongyi;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class is used to manage the checkpoint of the training process.
 */
public class SimulatedAnnealingCheckPointManager {
    private final String mCheckPointPath;

    /**
     * Constructor.
     *
     * @param checkPointPath The path of the checkpoint file.
     */
    public SimulatedAnnealingCheckPointManager(final String checkPointPath) {
        mCheckPointPath = checkPointPath;
    }

    /**
     * Save the checkpoint as a JSON file.
     */
    public void save(final SimulatedAnnealingCheckPointData data) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("temperature", data.getTemperature());
        jsonObject.put("decayRate", data.getDecayRate());
        jsonObject.put("currentHyperParameters", data.getCurrentHyperParameters());
        jsonObject.put("currentScore", data.getCurrentScore());
        jsonObject.put("currentHyperParametersIndex", data.getCurrentHyperParametersIndex());
        jsonObject.put("computedScoresInGroup", data.getComputedScoresInGroup());
        jsonObject.put("currentHyperParameterValues", data.getCurrentHyperParameterValues());
        jsonObject.put("bestHyperParameters", data.getBestHyperParameters());
        jsonObject.put("bestScore", data.getBestScore());
        try {
            FileWriter fileWriter = new FileWriter(mCheckPointPath);
            fileWriter.write(jsonObject.toString());
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, String> jsonObjectToMap(JSONObject jsonObject) {
        Map<String, String> map = new HashMap<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = jsonObject.getString(key);
            map.put(key, value);
        }
        return map;
    }

    public SimulatedAnnealingCheckPointData load() {
        try {
            String jsonString = new String(Files.readAllBytes(Paths.get(mCheckPointPath)));
            JSONObject jsonObject = new JSONObject(jsonString);
            float temperature = (float) jsonObject.getDouble("temperature");
            float decayRate = (float) jsonObject.getDouble("decayRate");
            Map<String, String> currentHyperParameters = jsonObjectToMap(jsonObject.getJSONObject(
                    "currentHyperParameters"));
            float currentScore = (float) jsonObject.getDouble("currentScore");
            int currentHyperParametersIndex = jsonObject.getInt("currentHyperParametersIndex");
            List<Float> computedScoresInGroup =
                    jsonObject.getJSONArray("computedScoresInGroup").toList().stream()
                            .map(o -> ((Number) o).floatValue()).collect(Collectors.toList());
            List<Float> currentHyperParameterValues = jsonObject.getJSONArray(
                            "currentHyperParameterValues").toList().stream()
                    .map(o -> ((Number) o).floatValue()).collect(Collectors.toList());
            Map<String, String> bestHyperParameters = jsonObjectToMap(jsonObject.getJSONObject(
                    "bestHyperParameters"));
            float bestScore = (float) jsonObject.getDouble("bestScore");
            return new SimulatedAnnealingCheckPointData(temperature, decayRate,
                    currentHyperParameters, currentScore,
                    currentHyperParametersIndex, computedScoresInGroup, currentHyperParameterValues,
                    bestHyperParameters, bestScore);
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
