package com.hljunlp.laozhongyi;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.collect.Maps;

public class HyperParamResultManager {
    private static final HashMap<Pair<Map<String, String>, Integer>, Float> mResults = Maps
            .newHashMap();

    public static synchronized Optional<Float> getResult(final Map<String, String> hyperParams,
            final int triedTimes) {
        return Optional.ofNullable(mResults.get(ImmutablePair.of(hyperParams, triedTimes)));
    }

    public static synchronized void putResult(final Map<String, String> hyperParams,
            final int triedTimes, final float result) {
        final Pair<Map<String, String>, Integer> pair = ImmutablePair.of(hyperParams, triedTimes);
        mResults.put(pair, result);
    }
}
