package com.hljunlp.laozhongyi;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class HyperParameterConfig {
    private final String mConfigFilePath;

    public HyperParameterConfig(final String configFilePath) {
        mConfigFilePath = configFilePath;
    }

    public void write(final Map<String, String> items) {
        final List<String> lines = Lists.newArrayList();
        for (final Entry<String, String> item : items.entrySet()) {
            lines.add(item.getKey() + " = " + item.getValue());
        }

        try {
            FileUtils.writeLines(new File(mConfigFilePath), lines);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void check(final Map<String, String> items) {
        final Map<String, String> map = toMap();
        Preconditions.checkState(map.size() == items.size());
        for (final Entry<String, String> item : items.entrySet()) {
            Preconditions.checkState(map.get(item.getKey()) == item.getValue());
        }
    }

    private Map<String, String> toMap() {
        final File file = new File(mConfigFilePath);
        List<String> lines;
        try {
            lines = FileUtils.readLines(file, Charsets.UTF_8);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
        final Map<String, String> map = Maps.newTreeMap();
        for (final String line : lines) {
            final String[] segs = StringUtils.split(line, " = ");
            map.put(segs[0], segs[1]);
        }

        return map;
    }
}
