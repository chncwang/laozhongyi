package com.hljunlp.laozhongyi.strategy;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class SimulatedAnnealingStrategyTest {
    @Test
    public void test() {
        final List<Float> probs = Lists.newArrayList();
        probs.add((float) 0.5);
        for (int i = 0; i < 9; ++i) {
            probs.add((float) 0.49);
        }

        final Map<String, Float> params = Maps.newTreeMap();
        params.put("r", 0.9f);
        params.put("t", 1.0f);
        final Strategy strategy = Strategy.valueOf("sa", params);
        for (int i = 0; i < 1000; ++i) {
            final int index = strategy.chooseSuitableIndex(probs);
            System.out.println(i + ":" + index);
            strategy.iterationEnd();
        }
    }
}
