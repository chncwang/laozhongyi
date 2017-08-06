package com.hljunlp.laozhongyi;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;

public class HyperParamResultManager {
    private static final Map<Map<String, String>, Float> mResults = Maps.newHashMap();

    public static synchronized Optional<Float> getResult(final Map<String, String> hyperParams) {
        return Optional.ofNullable(mResults.get(hyperParams));
    }

    public static synchronized void putResult(final Map<String, String> hyperParams,
            final float result) {
        mResults.put(hyperParams, result);
    }
}
