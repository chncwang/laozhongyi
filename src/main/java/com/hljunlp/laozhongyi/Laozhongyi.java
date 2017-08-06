package com.hljunlp.laozhongyi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hljunlp.laozhongyi.strategy.BaseStrategy;
import com.hljunlp.laozhongyi.strategy.Strategy;
import com.hljunlp.laozhongyi.strategy.TraiditionalSimulatedAnnealingStrategy;
import com.hljunlp.laozhongyi.strategy.VariantSimulatedAnnealingStrategy;

public class Laozhongyi {
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

        ExecutorService executorService = null;
        try {
            executorService = Executors.newFixedThreadPool(8);

            final Random random = new Random();
            Map<String, String> params = initHyperParameters(items, random);
            final Set<String> multiValueKeys = getMultiValueKeys(items);
            while (true) {
                String modifiedKey = StringUtils.EMPTY;
                for (final HyperParameterScopeItem item : items) {
                    Preconditions.checkState(!item.getValues().isEmpty());
                    if (item.getValues().size() == 1) {
                        continue;
                    }
                    System.out.println("item:" + item);
                    final Pair<String, Float> result = tryItem(item, multiValueKeys, params,
                            programCmd, executorService, strategy, bestPair,
                            Optional.ofNullable(workingDirStr), runtimeMinutes);
                    System.out.println("key:" + item.getKey() + "\nsuitable value:"
                            + result.getLeft() + " result:" + result.getRight());
                    if (!result.getLeft().equals(params.get(item.getKey()))) {
                        modifiedKey = item.getKey();
                        params.put(item.getKey(), result.getLeft());
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
            System.out
                    .println("hyperparameter adjusted, the best result is " + bestPair.getRight());
            System.out.println("best hyperparameteres:");
            for (final Entry<String, String> entry : bestPair.getLeft().entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
        } catch (final RuntimeException e) {
            e.printStackTrace();
            throw e;
        } finally {
            executorService.shutdown();
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

    private static Pair<String, Float> tryItem(final HyperParameterScopeItem item,
            final Set<String> multiValueKeys, final Map<String, String> currentHyperParameter,
            final String cmdString, final ExecutorService executorService, final Strategy strategy,
            final MutablePair<Map<String, String>, Float> best, final Optional<String> workingDir,
            final int runtimeMinutes) {
        Preconditions.checkArgument(item.getValues().size() > 1);

        final List<Future<Float>> futures = Lists.newArrayList();

        for (final String value : item.getValues()) {
            final Future<Float> future = executorService.submit(new Callable<Float>() {
                @Override
                public Float call() {
                    final Map<String, String> copiedHyperParameter = Maps.newTreeMap();
                    for (final Entry<String, String> entry : currentHyperParameter.entrySet()) {
                        copiedHyperParameter.put(entry.getKey(), entry.getValue());
                    }
                    copiedHyperParameter.put(item.getKey(), value);

                    final Optional<Float> cachedResult = HyperParamResultManager
                            .getResult(copiedHyperParameter);
                    if (cachedResult.isPresent()) {
                        return cachedResult.get();
                    }

                    final String configFilePath = GeneratedFileManager
                            .getHyperParameterConfigFileFullPath(copiedHyperParameter,
                                    multiValueKeys);
                    final String newCmdString = cmdString.replace("{}", configFilePath);
                    final HyperParameterConfig config = new HyperParameterConfig(configFilePath);
                    config.write(copiedHyperParameter);
                    final String logFileFullPath = GeneratedFileManager
                            .getLogFileFullPath(copiedHyperParameter, multiValueKeys);
                    System.out.println("logFileFullPath:" + logFileFullPath);
                    try (OutputStream os = new FileOutputStream(logFileFullPath)) {
                        final DefaultExecutor executor = new DefaultExecutor();
                        executor.setStreamHandler(new PumpStreamHandler(os));
                        if (workingDir.isPresent()) {
                            executor.setWorkingDirectory(new File(workingDir.get()));
                        }
                        final ExecuteWatchdog dog = new ExecuteWatchdog(60000 * runtimeMinutes);
                        executor.setWatchdog(dog);
                        System.out.println("begin to execute " + newCmdString);
                        final org.apache.commons.exec.CommandLine commandLine = org.apache.commons.exec.CommandLine
                                .parse(newCmdString);
                        try {
                            executor.execute(commandLine);
                        } catch (final ExecuteException e) {
                            System.out.println(e.getMessage());
                        } catch (final IOException e) {
                            throw new IllegalStateException(e);
                        }

                        final String log = FileUtils.readFileToString(new File(logFileFullPath),
                                Charsets.UTF_8);
                        final float result = logResult(log);

                        HyperParamResultManager.putResult(copiedHyperParameter, result);

                        return result;
                    } catch (final RuntimeException e) {
                        e.printStackTrace();
                        throw e;
                    } catch (final IOException e) {
                        e.printStackTrace();
                        throw new IllegalStateException(e);
                    }
                }
            });
            futures.add(future);
        }

        final List<Float> results = Lists.newArrayList();
        for (int i = 0; i < futures.size(); ++i) {
            final float futureResult;
            try {
                futureResult = futures.get(i).get();
                System.out.println("key:" + item.getKey() + " value:" + item.getValues().get(i)
                        + " futureResult:" + futureResult);
            } catch (final InterruptedException e) {
                throw new IllegalStateException(e);
            } catch (final ExecutionException e) {
                throw new IllegalStateException(e);
            }

            results.add(futureResult);
        }

        float bestResult = -1;
        int bestIndex = -1;
        for (int i = 0; i < results.size(); ++i) {
            if (results.get(i) > bestResult) {
                bestResult = results.get(i);
                bestIndex = i;
            }
        }
        Preconditions.checkState(bestIndex != -1);

        synchronized (Laozhongyi.class) {
            if (bestResult > best.getRight()) {
                best.setRight(bestResult);

                final Map<String, String> bestParams = Maps.newTreeMap();
                for (final Entry<String, String> entry : currentHyperParameter.entrySet()) {
                    bestParams.put(entry.getKey(), entry.getValue());
                }
                bestParams.put(item.getKey(), item.getValues().get(bestIndex));

                best.setLeft(bestParams);
                strategy.storeBest();
            }
        }

        final String originalValue = currentHyperParameter.get(item.getKey());
        final int originalIndex = item.getValues().indexOf(originalValue);
        final int suitableIndex = strategy.chooseSuitableIndex(results, originalIndex);

        return ImmutablePair.of(item.getValues().get(suitableIndex), results.get(suitableIndex));
    }

    private static float logResult(final String log) {
        final Pattern pattern = Pattern.compile("laozhongyi_([\\d\\.]+)");
        final Matcher matcher = pattern.matcher(log);
        float result = 0.0f;
        while (matcher.find()) {
            final String string = matcher.group(1);
            result = Float.valueOf(string);
        }
        return result;
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
