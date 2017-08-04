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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        final String programCmd = cmd.getOptionValue("c");

        GeneratedFileManager.mkdirForLog();
        GeneratedFileManager.mkdirForHyperParameterConfig();

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
                            programCmd, executorService);
                    System.out.println("key:" + item.getKey() + "\nbest value:" + result.getLeft()
                            + " result:" + result.getRight());
                    params.put(item.getKey(), result.getLeft());
                    System.out.println("complete params now:\n");
                    for (final Entry<String, String> entry : params.entrySet()) {
                        System.out.println(entry.getKey() + ": " + entry.getValue());
                    }

                    if (result.getRight() > bestResult) {
                        bestResult = result.getRight();
                        hitBestKey = item.getKey();
                    }
                }
            }
            System.out.println("hyperparameter adjusted, the best result is " + bestResult);
            System.out.println("best hyperparameteres:\n");
            for (final Entry<String, String> entry : params.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
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
            final String cmdString, final ExecutorService executorService) {
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
                        final ExecuteWatchdog dog = new ExecuteWatchdog(3600000);
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
                System.out.println("futreResult:" + futureResult);
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
        final Pattern pattern = Pattern.compile("test and dev avg is ([\\d\\.]+)");
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
