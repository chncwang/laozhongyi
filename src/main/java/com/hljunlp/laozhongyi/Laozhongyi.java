package com.hljunlp.laozhongyi;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class Laozhongyi {
    public static void main(final String[] args) {
        final Options options = new Options();
        final Option scope = new Option("s", true, "scope file path");
        scope.setRequired(true);
        options.addOption(scope);

        final Option hyper = new Option("h", true, "hyper parameter file path");
        hyper.setRequired(true);
        options.addOption(hyper);

        final Option cmdOpt = new Option("c", true, "cmd");
        cmdOpt.setRequired(true);
        options.addOption(cmdOpt);

        final CommandLineParser parser = new DefaultParser();
        final CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (final ParseException e) {
            throw new IllegalStateException(e);
        }

        final Option[] parsedOptions = cmd.getOptions();
        for (final Option option : parsedOptions) {
            System.out.println(option.getOpt() + ":" + option.getValue());
        }

        final String scopeFilePath = cmd.getOptionValue("s");
        final List<HyperParameterScopeItem> items = HyperParameterScopeConfigReader
                .read(scopeFilePath);

        final String hyperParameterConfigFilePath = cmd.getOptionValue("h");
        final HyperParameterConfig hyperParameterConfig = new HyperParameterConfig(
                hyperParameterConfigFilePath);

        final String programCmd = cmd.getOptionValue("c");

        LogFileManager.mkdir();

        ExecutorService executorService = null;
        try {
            executorService = Executors.newFixedThreadPool(8);
            float bestResult = -1;
            String hitBestKey = StringUtils.EMPTY;

            final Random random = new Random();
            final Map<String, String> params = initHyperParameters(items, random);
            final Set<String> multiValueKeys = getMultiValueKeys(items);
            boolean shouldStop = false;
            while (!shouldStop) {
                for (final HyperParameterScopeItem item : items) {
                    if (hitBestKey.equals(item.getKey())) {
                        shouldStop = true;
                        break;
                    }

                    Preconditions.checkState(!item.getValues().isEmpty());
                    if (item.getValues().size() == 1) {
                        continue;
                    }
                    System.out.println("item:" + item);
                    final Pair<String, Float> result = tryItem(item, multiValueKeys, params,
                            hyperParameterConfig, programCmd, executorService);
                    System.out.println("key:" + item.getKey() + "\nbest value:" + result.getLeft()
                            + " result:" + result.getRight());
                    params.put(item.getKey(), result.getLeft());
                    System.out.println("complete params now:\n" + ToStringBuilder
                            .reflectionToString(params, ToStringStyle.MULTI_LINE_STYLE));

                    if (result.getRight() > bestResult) {
                        bestResult = result.getRight();
                        hitBestKey = item.getKey();
                    }
                }
            }
            System.out.println("hyperparameter adjusted, the best result is " + bestResult);
            System.out.println("best hyperparameteres:\n"
                    + ToStringBuilder.reflectionToString(params, ToStringStyle.MULTI_LINE_STYLE));
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
            final HyperParameterConfig hyperParameterConfig, final String cmdString,
            final ExecutorService executorService) {
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

                    final String logFileFullPath = LogFileManager
                            .getLogFileFullPath(copiedHyperParameter, multiValueKeys);
                    System.out.println("logFileFullPath:" + logFileFullPath);
                    try (OutputStream os = new FileOutputStream(logFileFullPath)) {
                        copiedHyperParameter.put(item.getKey(), value);
                        hyperParameterConfig.write(copiedHyperParameter);

                        final DefaultExecutor executor = new DefaultExecutor();
                        executor.setStreamHandler(new PumpStreamHandler(os));
                        final ExecuteWatchdog dog = new ExecuteWatchdog(3600000);
                        executor.setWatchdog(dog);
                        final org.apache.commons.exec.CommandLine commandLine = org.apache.commons.exec.CommandLine
                                .parse(cmdString);
                        try {
                            executor.execute(commandLine);
                        } catch (final ExecuteException e) {
                            throw new IllegalStateException(e);
                        } catch (final IOException e) {
                            throw new IllegalStateException(e);
                        }

                        final String log = FileUtils.readFileToString(new File(logFileFullPath),
                                Charsets.UTF_8);
                        final float result = logResult(log);
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

        float bestResult = -1;
        String bestValue = null;
        int i = -1;
        for (final Future<Float> future : futures) {
            ++i;
            final float futureResult;
            try {
                futureResult = future.get();
            } catch (final InterruptedException e) {
                throw new IllegalStateException(e);
            } catch (final ExecutionException e) {
                throw new IllegalStateException(e);
            }

            if (futureResult > bestResult) {
                bestResult = futureResult;
                bestValue = item.getValues().get(i);
            }
        }
        Preconditions.checkNotNull(bestValue);
        return ImmutablePair.of(bestValue, bestResult);
    }

    private static float logResult(final String log) {
        return 0.5f;
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
