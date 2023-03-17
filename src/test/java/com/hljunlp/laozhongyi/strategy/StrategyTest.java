package com.hljunlp.laozhongyi.strategy;

import java.util.List;

import org.junit.Test;

import com.google.common.collect.Lists;

public class StrategyTest {
    @Test
    public void testTraditionalSa() {
        final TraiditionalSimulatedAnnealingStrategy strategy = new TraiditionalSimulatedAnnealingStrategy(
                (float) 0.9, 1);
        final List<Float> results = Lists.newArrayList(0.5f, 0.49f);
        for (int i = 0; i < 100; ++i) {
            final int index = strategy.chooseSuitableIndex(results, 0, false);
            System.out.println(index);
            strategy.iterationEnd();
        }
    }
}
