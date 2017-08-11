package com.hljunlp.laozhongyi;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hljunlp.laozhongyi.process.ParamsAndCallable;
import com.hljunlp.laozhongyi.process.ProcessManager;
import com.hljunlp.laozhongyi.process.ShellProcess;
import com.hljunlp.laozhongyi.strategy.BaseStrategy;
import com.hljunlp.laozhongyi.strategy.Strategy;
import com.hljunlp.laozhongyi.strategy.TraiditionalSimulatedAnnealingStrategy;
import com.hljunlp.laozhongyi.strategy.VariantSimulatedAnnealingStrategy;

public class Laozhongyi {
    private static final int PROCESS_COUNT_LIMIT = 8;

    public static void main(final String[] args) {
        final Options options = new Options();
        final Option scope = new Option("s", true, "scope file path");
        scope.setRequired(true);
        options.addOption(scope);

        final Option cmdOpt = new Option("c", true, "cmd");
        cmdOpt.setRequired(true);
        options.addOption(cmdOpt);

        final Option workingDir = new Option("wd", true, "working directory");
        workingDir.setRequired(false);
        options.addOption(workingDir);

        final Option strategyOpt = new Option("strategy", true, "base or sa or vsa");
        strategyOpt.setRequired(true);
        options.addOption(strategyOpt);

        final Option saTOpt = new Option("sat", true, "simulated annealing initial temperature");
        saTOpt.setRequired(false);
        options.addOption(saTOpt);

        final Option saROpt = new Option("sar", true, "simulated annealing ratio");
        saTOpt.setRequired(false);
        options.addOption(saROpt);

        final Option runtimeOpt = new Option("rt", true, "program runtime upper bound in minutes");
        runtimeOpt.setRequired(true);
        options.addOption(runtimeOpt);

        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (final ParseException e) {
            final HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("laozhonghi", options);
            throw new IllegalStateException(e);
        }

        final Option[] parsedOptions = cmd.getOptions();
        for (final Option option : parsedOptions) {
            System.out.println(option.getOpt() + ":" + option.getValue());
        }

        final String scopeFilePath = cmd.getOptionValue("s");
        final List<HyperParameterScopeItem> items = HyperParameterScopeConfigReader
                .read(scopeFilePath);

        final Strategy strategy;
        final String strategyStr = cmd.getOptionValue("strategy");
        if (strategyStr.equals("base")) {
            strategy = new BaseStrategy();
        } else if (strategyStr.equals("sa")) {
            final float ratio = Float.valueOf(cmd.getOptionValue("sar"));
            final float t = Float.valueOf(cmd.getOptionValue("sat"));
            strategy = new TraiditionalSimulatedAnnealingStrategy(ratio, t);
        } else if (strategyStr.equals("vsa")) {
            final float ratio = Float.valueOf(cmd.getOptionValue("sar"));
            final float t = Float.valueOf(cmd.getOptionValue("sat"));
            strategy = new VariantSimulatedAnnealingStrategy(ratio, t);
        } else {
            throw new IllegalArgumentException("strategy param is " + strategyStr);
        }

        final String workingDirStr = cmd.getOptionValue("wd");

        final String programCmd = cmd.getOptionValue("c");

        final int runtimeMinutes = Integer.valueOf(cmd.getOptionValue("rt"));

        GeneratedFileManager.mkdirForLog();
        GeneratedFileManager.mkdirForHyperParameterConfig();

        final MutablePair<Map<String, String>, Float> bestPair = MutablePair
                .of(Collections.emptyMap(), -1.f);

        final Random random = new Random();
        Map<String, String> params = initHyperParameters(items, random);
        final Set<String> multiValueKeys = getMultiValueKeys(items);
        final ExecutorService executorService = Executors.newFixedThreadPool(PROCESS_COUNT_LIMIT);
        final ProcessManager processManager = new ProcessManager(runtimeMinutes);

        while (true) {
            String modifiedKey = StringUtils.EMPTY;
            for (final HyperParameterScopeItem item : items) {
                Preconditions.checkState(!item.getValues().isEmpty());
                if (item.getValues().size() == 1) {
                    continue;
                }
                System.out.println("item:" + item);
                final Pair<Map<String, String>, Float> result = tryItem(item, multiValueKeys,
                        params, programCmd, executorService, strategy, bestPair,
                        Optional.ofNullable(workingDirStr), runtimeMinutes, processManager);
                System.out.println("key:" + item.getKey() + "\nsuitable value:" + result.getLeft()
                        + " result:" + result.getRight());
                if (!result.getLeft().equals(params.get(item.getKey()))) {
                    modifiedKey = item.getKey();
                    params = result.getLeft();
                }
                System.out.println("suitable params now:");
                for (final Entry<String, String> entry : params.entrySet()) {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                }
                System.out.println("best result:" + bestPair.getRight());
                System.out.println("best params now:");
                for (final Entry<String, String> entry : bestPair.getLeft().entrySet()) {
                    System.out.println(entry.getKey() + ": " + entry.getValue());
                }
            }
            strategy.iterationEnd();

            if (modifiedKey.equals(StringUtils.EMPTY)) {
                if (params.equals(bestPair.getLeft())) {
                    if (strategy.ensureIfStop(true)) {
                        break;
                    }
                } else {
                    params = bestPair.getLeft();
                    strategy.restoreBest();
                }
            }
        }

        while (true) {
            final Optional<Pair<List<ParamsAndCallable>, Integer>> pairOptional = processManager
                    .removeCallables();
            if (!pairOptional.isPresent()) {
                break;
            }
            final Pair<List<ParamsAndCallable>, Integer> pair = pairOptional.get();
            final List<ParamsAndCallable> left = pair.getLeft();
            final List<Future<Pair<Float, Boolean>>> futures = Lists.newArrayList();
            for (final ParamsAndCallable paramsAndCallable : left) {
                final ShellProcess callable = paramsAndCallable.getCallable();
                final Future<Pair<Float, Boolean>> future = executorService.submit(callable);
                futures.add(future);
            }

            for (int i = 0; i < futures.size(); ++i) {
                final Pair<Float, Boolean> futureResult;
                try {
                    futureResult = futures.get(i).get();
                    System.out.println("futureResult:" + futureResult);
                    final ParamsAndCallable paramsAndCallable = left.get(i);
                    final Map<String, String> p = paramsAndCallable.getParams();
                    for (final Map.Entry<String, String> entry : p.entrySet()) {
                        System.out.println("key:" + entry.getKey() + " value:" + entry.getValue());
                    }

                    if (futureResult.getRight()) {
                        processManager.addToLongerRuntimeWaitingList(paramsAndCallable);
                    }

                    synchronized (Laozhongyi.class) {
                        if (bestPair.getRight() < futureResult.getLeft()) {
                            bestPair.setRight(futureResult.getLeft());
                            bestPair.setLeft(paramsAndCallable.getParams());

                            System.out.println("bestResult:" + bestPair.getRight());
                            for (final Map.Entry<String, String> entry : bestPair.getLeft()
                                    .entrySet()) {
                                System.out.println(
                                        "key:" + entry.getKey() + " value:" + entry.getValue());
                            }
                        }
                    }
                } catch (final InterruptedException e) {
                    throw new IllegalStateException(e);
                } catch (final ExecutionException e) {
                    throw new IllegalStateException(e);
                }
            }
        }

        System.out.println("hyperparameter adjusted, the best result is " + bestPair.getRight());
        System.out.println("best hyperparameteres:");
        for (final Entry<String, String> entry : bestPair.getLeft().entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
    }

    private static Map<String, String> initHyperParameters(
            final List<HyperParameterScopeItem> items, final Random random) {
        final Map<String, String> result = Maps.newTreeMap();
        for (final HyperParameterScopeItem item : items) {
            final int index = random.nextInt(item.getValues().size());
            result.put(item.getKey(), item.getValues().get(index));
        }
        return result;
    }

    private static Pair<Map<String, String>, Float> tryItem(final HyperParameterScopeItem item,
            final Set<String> multiValueKeys, final Map<String, String> currentHyperParameter,
            final String cmdString, final ExecutorService executorService, final Strategy strategy,
            final MutablePair<Map<String, String>, Float> best, final Optional<String> workingDir,
            final int runtimeMinutes, final ProcessManager processManager) {
        Preconditions.checkArgument(item.getValues().size() > 1);

        final List<Future<Pair<Float, Boolean>>> futures = Lists.newArrayList();
        final List<ParamsAndCallable> paramsAndCallables = Lists.newArrayList();

        for (final String value : item.getValues()) {
            final Map<String, String> copiedHyperParameter = Utils
                    .modifiedNewMap(currentHyperParameter, item.getKey(), value);

            final ShellProcess callable = new ShellProcess(copiedHyperParameter, multiValueKeys,
                    cmdString, workingDir);
            final Future<Pair<Float, Boolean>> future = executorService.submit(callable);
            futures.add(future);
            paramsAndCallables.add(new ParamsAndCallable(copiedHyperParameter, callable));
        }

        final List<Float> results = Lists.newArrayList();
        for (int i = 0; i < futures.size(); ++i) {
            final Pair<Float, Boolean> futureResult;
            try {
                futureResult = futures.get(i).get();
                System.out.println("key:" + item.getKey() + " value:"
                        + paramsAndCallables.get(i).getParams().get(item.getKey())
                        + " futureResult:" + futureResult);
                if (futureResult.getRight()) {
                    processManager.addToLongerRuntimeWaitingList(paramsAndCallables.get(i));
                }
            } catch (final InterruptedException e) {
                throw new IllegalStateException(e);
            } catch (final ExecutionException e) {
                throw new IllegalStateException(e);
            }

            results.add(futureResult.getLeft());
        }

        float localBestResult = -1;
        Map<String, String> localBestParams = Collections.emptyMap();
        for (int i = 0; i < results.size(); ++i) {
            if (results.get(i) > localBestResult) {
                localBestResult = results.get(i);
                localBestParams = paramsAndCallables.get(i).getParams();
            }
        }
        Preconditions.checkState(!localBestParams.isEmpty());

        synchronized (Laozhongyi.class) {
            if (localBestResult > best.getRight()) {
                best.setRight(localBestResult);
                best.setLeft(localBestParams);
                strategy.storeBest();
            }
        }

        final String originalValue = currentHyperParameter.get(item.getKey());
        final int originalIndex = item.getValues().indexOf(originalValue);
        final int suitableIndex = strategy.chooseSuitableIndex(results, originalIndex);

        return ImmutablePair.of(paramsAndCallables.get(suitableIndex).getParams(),
                results.get(suitableIndex));
    }

    private static Set<String> getMultiValueKeys(final List<HyperParameterScopeItem> items) {
        final Set<String> keys = Sets.newTreeSet();
        for (final HyperParameterScopeItem item : items) {
            if (item.getValues().size() > 1) {
                keys.add(item.getKey());
            }
        }

        return keys;
    }
}
