package com.hljunlp.laozhongyi.process;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.hljunlp.laozhongyi.HyperParamResultManager;

public class ProcessManager {
    public static final int INITIAL_RUNTIME_IN_MINUTES = 5;
    private static final int K = 2;
    private final int mProcessRuntimeLimit;
    private final Map<Integer, List<ParamsAndCallable>> mRunnableMap = Maps.newTreeMap();

    public ProcessManager(final int processRuntimeLimit) {
        mProcessRuntimeLimit = processRuntimeLimit;
        for (int i = 1; i < 100; ++i) {
            mRunnableMap.put(i, Lists.newArrayList());
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

        final List<ParamsAndCallable> waitingList = mRunnableMap.get(triedTimes);
        Preconditions.checkNotNull(waitingList);

        waitingList.add(paramsAndCallable);
    }

    public synchronized Optional<Pair<List<ParamsAndCallable>, Integer>> removeCallables() {
        validate();
        for (int i = 1; i < 100; ++i) {
            final List<ParamsAndCallable> paramsAndCallables = mRunnableMap.get(i);
            final List<ParamsAndCallable> sortedParamsAndCallables = paramsAndCallables.stream()
                    .sorted((a, b) -> Float.compare(HyperParamResultManager
                            .getResult(b.getParams(), b.getCallable().getTriedTimes()).get(),
                            HyperParamResultManager
                                    .getResult(a.getParams(), a.getCallable().getTriedTimes())
                                    .get()))
                    .collect(Collectors.toList());

            if (!sortedParamsAndCallables.isEmpty()) {
                final List<ParamsAndCallable> removed = Lists.newArrayList();
                mRunnableMap.put(i, removed);

                return Optional.of(ImmutablePair.of(sortedParamsAndCallables, i));
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
