package com.hljunlp.laozhongyi.strategy;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class SimulatedAnnealingStrategyTest {
    @Test
    public void test() {
        final List<Float> probs = Lists.newArrayList();
        probs.add((float) 0.5);
        for (int i = 0; i < 9; ++i) {
            probs.add((float) 0.49);
        }

        final Strategy strategy = new VariantSimulatedAnnealingStrategy((float) 0.9, 1);
        for (int i = 0; i < 1000; ++i) {
            final int index = strategy.chooseSuitableIndex(probs, 0);
            System.out.println(i + ":" + index);
            strategy.iterationEnd();
        }
    }
}
