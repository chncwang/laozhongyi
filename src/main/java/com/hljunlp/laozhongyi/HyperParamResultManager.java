package com.hljunlp.laozhongyi;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class HyperParamResultManager {
    private static final Map<Map<String, String>, Float> mResults = Maps.newHashMap();

    public static synchronized Optional<Float> getResult(final Map<String, String> hyperParams) {
        return Optional.ofNullable(mResults.get(hyperParams));
    }

    public static synchronized void putResult(final Map<String, String> hyperParams,
                                              final float result) {
        mResults.put(hyperParams, result);
    }

    public static Map<Map<String, String>, Float> deepCopyResults() {
        Map<Map<String, String>, Float> copy = new HashMap<>();
        synchronized (HyperParamResultManager.class) {
            for (Map.Entry<Map<String, String>, Float> entry : mResults.entrySet()) {
                Map<String, String> hyperParamsCopy = new HashMap<>(entry.getKey());
                Float resultCopy = entry.getValue();
                copy.put(hyperParamsCopy, resultCopy);
            }
        }
        return copy;
    }
}
