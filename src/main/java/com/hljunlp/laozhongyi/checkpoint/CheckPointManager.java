package com.hljunlp.laozhongyi.checkpoint;

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

public class CheckPointManager {
    private final String mCheckPointPath;

    /**
     * Constructor.
     *
     * @param checkPointPath The path of the checkpoint file.
     */
    public CheckPointManager(final String checkPointPath) {
        mCheckPointPath = checkPointPath;
    }

    protected JSONObject convertToJSON(final CheckPointData data) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("currentHyperParameters", data.getCurrentHyperParameters());
        jsonObject.put("currentScore", data.getCurrentScore());
        jsonObject.put("currentHyperParametersIndex", data.getCurrentHyperParametersIndex());
        jsonObject.put("computedScoresInGroup", data.getComputedScoresInGroup());
        jsonObject.put("currentHyperParameterValues", data.getCurrentHyperParameterValues());
        jsonObject.put("bestHyperParameters", data.getBestHyperParameters());
        jsonObject.put("bestScore", data.getBestScore());
        return jsonObject;
    }

    /**
     * Save the checkpoint as a JSON file.
     */
    public void save(final CheckPointData data) {
        JSONObject jsonObject = convertToJSON(data);
        try {
            FileWriter fileWriter = new FileWriter(mCheckPointPath);
            fileWriter.write(jsonObject.toString());
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected static Map<String, String> jsonObjectToMap(JSONObject jsonObject) {
        Map<String, String> map = new HashMap<>();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            String value = jsonObject.getString(key);
            map.put(key, value);
        }
        return map;
    }

    protected CheckPointData convertFromJSON(JSONObject jsonObject) {
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
        return new CheckPointData(currentHyperParameters, currentScore,
                currentHyperParametersIndex, computedScoresInGroup,
                currentHyperParameterValues, bestHyperParameters, bestScore);
    }

    public CheckPointData load() {
        try {
            String jsonString = new String(Files.readAllBytes(Paths.get(mCheckPointPath)));
            JSONObject jsonObject = new JSONObject(jsonString);
            return convertFromJSON(jsonObject);
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
