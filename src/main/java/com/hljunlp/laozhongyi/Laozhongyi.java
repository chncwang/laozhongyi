package com.hljunlp.laozhongyi;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

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

        final Random random = new Random();
        final Map<String, String> params = initHyperParameters(items, random);
        for (final HyperParameterScopeItem item : items) {
            Preconditions.checkState(!item.getValues().isEmpty());
            if (item.getValues().size() == 1) {
                continue;
            }
            tryItem(item, params, hyperParameterConfig, programCmd);
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

    private static void tryItem(final HyperParameterScopeItem item,
            final Map<String, String> currentHyperParameter,
            final HyperParameterConfig hyperParameterConfig, final String programCmd) {
        Preconditions.checkArgument(item.getValues().size() > 1);
        for (final String value : item.getValues()) {
            currentHyperParameter.put(item.getKey(), value);
            hyperParameterConfig.write(currentHyperParameter);
            final String logFileFullPath = LogFileManager.getLogFileFullPath(currentHyperParameter);

        }
    }
}
