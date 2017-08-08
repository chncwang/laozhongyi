package com.hljunlp.laozhongyi.process;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ProcessManager {
    public static final int INITIAL_RUNTIME_IN_MINUTES = 5;
    private static final int K = 2;
    private final int mProcessCountLimit;
    private final int mProcessRuntimeLimit;
    private final Map<Integer, List<ParamsAndCallable>> mRunnableMap = Maps.newTreeMap();

    public ProcessManager(final int processCountLimit, final int processRuntimeLimit) {
        mProcessCountLimit = processCountLimit;
        mProcessRuntimeLimit = processRuntimeLimit;
        for (int i = 1; i < 100; ++i) {
            mRunnableMap.put(i, Collections.emptyList());
        }
    }

    public synchronized void addToLongerRuntimeWaitingList(
            final ParamsAndCallable paramsAndCallable) {
        validate();
        final int triedTimes = paramsAndCallable.getCallable().getTriedTimes();
        Preconditions.checkArgument(triedTimes >= 1);
        final int nextRuntimeLimit = runtimeLimitInMinutes(triedTimes);
        if (nextRuntimeLimit > mProcessRuntimeLimit) {
            System.out.println("callable discarded");
            return;
        }

        List<ParamsAndCallable> waitingList = mRunnableMap.get(triedTimes);
        if (waitingList == null) {
            waitingList = Lists.newArrayList();
            mRunnableMap.put(triedTimes, waitingList);
        }

        waitingList.add(paramsAndCallable);
    }

    public synchronized Optional<Pair<List<ParamsAndCallable>, Integer>> timeToRunCallables() {
        validate();
        final List<ParamsAndCallable> result = Lists.newArrayList();
        for (int i = 1; i < 100; ++i) {
            final List<ParamsAndCallable> paramsAndCallables = mRunnableMap.get(i);
            if (paramsAndCallables.size() >= mProcessCountLimit) {
                for (int j = 0; j < mProcessCountLimit; ++j) {
                    result.add(paramsAndCallables.get(j));
                }

                final List<ParamsAndCallable> removed = Lists.newArrayList();
                for (int j = mProcessCountLimit; j < paramsAndCallables.size(); ++j) {
                    removed.add(paramsAndCallables.get(j));
                }
                mRunnableMap.put(i, removed);

                return Optional.of(ImmutablePair.of(result, i));
            }
        }

        return Optional.empty();
    }

    public synchronized void validate() {
        for (final Map.Entry<Integer, List<ParamsAndCallable>> entry : mRunnableMap.entrySet()) {
            final List<ParamsAndCallable> paramsAndCallables = entry.getValue();
            for (final ParamsAndCallable paramsAndCallable : paramsAndCallables) {
                Preconditions.checkState(
                        paramsAndCallable.getCallable().getTriedTimes() == entry.getKey());
            }
        }
    }

    public static int runtimeLimitInMinutes(final int triedTimes) {
        int product = INITIAL_RUNTIME_IN_MINUTES;
        for (int i = 0; i < triedTimes; ++i) {
            product *= K;
        }
        return product;
    }
}
