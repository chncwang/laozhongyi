package com.hljunlp.laozhongyi;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import com.google.common.collect.Lists;

public class GeneratedFileManager {
    private static String mLogDirPath;
    private static String mHyperParameterConfigDirPath;

    public static void mkdirForHyperParameterConfig() {
        final String homeDir = System.getProperty("user.home");
        final String logDir = "hyper" + new LocalDateTime().toString();
        mHyperParameterConfigDirPath = FilenameUtils.concat(homeDir, logDir);
        try {
            FileUtils.forceMkdir(new File(mHyperParameterConfigDirPath));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void mkdirForLog() {
        final String homeDir = System.getProperty("user.home");
        final String logDir = "log" + new LocalDateTime().toString();
        mLogDirPath = FilenameUtils.concat(homeDir, logDir);
        try {
            FileUtils.forceMkdir(new File(mLogDirPath));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String getHyperParameterConfigFileFullPath(final Map<String, String> config,
            final Set<String> multiValuesKeys) {
        final List<String> keys = Lists.newArrayList();
        for (final String key : config.keySet()) {
            if (multiValuesKeys.contains(key)) {
                keys.add(key);
            }
        }
        Collections.sort(keys);

        String fileName = StringUtils.EMPTY;
        for (final String key : keys) {
            fileName += key + config.get(key);
        }

        return FilenameUtils.concat(mHyperParameterConfigDirPath, fileName);
    }

    public static String getLogFileFullPath(final Map<String, String> config,
            final Set<String> multiValuesKeys) {
        final List<String> keys = Lists.newArrayList();
        for (final String key : config.keySet()) {
            if (multiValuesKeys.contains(key)) {
                keys.add(key);
            }
        }
        Collections.sort(keys);

        String fileName = StringUtils.EMPTY;
        for (final String key : keys) {
            fileName += key + config.get(key);
        }

        return FilenameUtils.concat(mLogDirPath, fileName);
    }
}
