package com.hljunlp.laozhongyi.checkpoint;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class CheckPointManager {
    private final String mCheckPointPath;
    private CheckPointData mCheckPointData;

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
        jsonObject.put("currentHyperParametersIndex", data.getCurrentHyperParametersIndex());
        jsonObject.put("bestHyperParameters", data.getBestHyperParameters());
        jsonObject.put("bestScore", data.getBestScore());
        Map<String, String> hyperParametersToScore = new HashMap<>();
        for (Map.Entry<Map<String, String>, Float> entry : data.getHyperParametersToScore()
                .entrySet()) {
            hyperParametersToScore.put(mapToString(entry.getKey()), entry.getValue().toString());
        }
        jsonObject.put("hyperParametersToScore", hyperParametersToScore);
        return jsonObject;
    }

    /**
     * Save the checkpoint as a JSON file.
     */
    public synchronized void save(final CheckPointData data) {
        String savePath = mCheckPointPath + "." + System.currentTimeMillis();
        save(data, savePath);
    }

    public synchronized void save(final CheckPointData data, final String savePath) {
        mCheckPointData = data.deepCopy();
        JSONObject jsonObject = convertToJSON(data);
        try {
            FileWriter fileWriter = new FileWriter(savePath);
            fileWriter.write(jsonObject.toString());
            fileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void saveIncrementally() {
        if (mCheckPointData == null) {
            return;
        }
        mCheckPointData = mCheckPointData.addHyperParameterToScoreMap();
        save(mCheckPointData);
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
        int currentHyperParametersIndex = jsonObject.getInt("currentHyperParametersIndex");
        Map<String, String> bestHyperParameters = jsonObjectToMap(jsonObject.getJSONObject(
                "bestHyperParameters"));
        float bestScore = (float) jsonObject.getDouble("bestScore");

        Map<Map<String, String>, Float> hyperParametersToScore = new HashMap<>();
        if (jsonObject.has("hyperParametersToScore")) {
            JSONObject hyperParametersToScoreJSONObject = jsonObject.getJSONObject(
                    "hyperParametersToScore");
            Map<String, Object> map = hyperParametersToScoreJSONObject.toMap();

            for (Map.Entry<String, Object> entry : map.entrySet()) {
                String keyString = entry.getKey();
                Float value = Float.parseFloat(entry.getValue().toString());
                Map<String, String> key = stringToMap(keyString);
                hyperParametersToScore.put(key, value);
            }
        }

        return new CheckPointData(currentHyperParameters, currentHyperParametersIndex,
                bestHyperParameters, bestScore, hyperParametersToScore);
    }

    public CheckPointData load() {
        return load(mCheckPointPath);
    }

    public static File getFileWithLargestSuffix(String fullFileName) {
        final String dir = fullFileName.substring(0, fullFileName.lastIndexOf(File.separator));
        System.out.println("getFileWithLargestSuffix: dir = " + dir);
        File[] files = Objects.requireNonNull(new File(dir).listFiles());
        System.out.println("getFileWithLargestSuffix: files = " + Arrays.toString(files));
        final List<String> fullFileNamesUnderDir = new ArrayList<>();
        for (File file : files) {
            System.out.println("getFileWithLargestSuffix: file = " + file);
            if (file.getName().startsWith(fullFileName)) {
                fullFileNamesUnderDir.add(file.getAbsolutePath());
            }
        }
        long largestSuffix = -1;
        String largestSuffixFileName = null;
        for (String fileName : fullFileNamesUnderDir) {
            String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
            long suffixLong = Long.parseLong(suffix);
            if (suffixLong > largestSuffix) {
                largestSuffix = suffixLong;
                largestSuffixFileName = fileName;
            }
        }
        if (largestSuffixFileName == null) {
            return null;
        } else {
            return new File(largestSuffixFileName);
        }
    }

    public CheckPointData load(String loadPath) {
        File file = getFileWithLargestSuffix(loadPath);
        if (file == null) {
            System.out.println("No checkpoint found at " + loadPath);
            return null;
        }
        try {
            System.out.println("Loading checkpoint from " + file.getAbsolutePath());
            String jsonString = new String(Files.readAllBytes(Paths.get(loadPath)));
            JSONObject jsonObject = new JSONObject(jsonString);
            return convertFromJSON(jsonObject);
        } catch (IOException | JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public static String mapToString(Map<String, String> passedMap) {
        // Create a TreeMap to fix the order of the keys in the string.
        Map<String, String> map = new TreeMap<>(passedMap);
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : map.entrySet()) {
            sb.append(entry.getKey());
            sb.append("=");
            sb.append(entry.getValue());
            sb.append(",");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public static Map<String, String> stringToMap(String str) {
        Map<String, String> map = new HashMap<>();
        String[] entries = str.split(",");
        for (String entry : entries) {
            String[] parts = entry.split("=");
            if (parts.length == 2) {
                map.put(parts[0], parts[1]);
            }
        }
        return map;
    }
}
