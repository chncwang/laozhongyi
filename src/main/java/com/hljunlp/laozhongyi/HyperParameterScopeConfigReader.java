package com.hljunlp.laozhongyi;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

public class HyperParameterScopeConfigReader {
    public static List<HyperParameterScopeItem> read(final String configFilePath) {
        final File file = new File(configFilePath);
        List<String> lines;
        try {
            lines = FileUtils.readLines(file, Charsets.UTF_8);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        final List<HyperParameterScopeItem> result = Lists.newArrayList();
        for (final String line : lines) {
            final String[] segments = StringUtils.split(line, ',');
            final List<String> values = Lists.newArrayList();
            for (int i = 1; i < segments.length; ++i) {
                values.add(segments[i]);
            }
            final HyperParameterScopeItem item = new HyperParameterScopeItem(segments[0], values);
            result.add(item);
        }
        return result;
    }
}
