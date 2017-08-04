package com.hljunlp.laozhongyi.strategy;

import java.util.List;

public interface Strategy {
    int chooseSuitableIndex(List<Float> results);

    void iterationEnd();
}
