package com.hljunlp.laozhongyi;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDateTime;

import com.google.common.collect.Lists;

public class LogFileManager {
    private static String mDirPath;

    public static void mkdir() {
        final String homeDir = System.getProperty("user.home");
        final String logDir = "log" + new LocalDateTime().toString();
        mDirPath = FilenameUtils.concat(homeDir, logDir);
        try {
            FileUtils.forceMkdir(new File(mDirPath));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static String getLogFileFullPath(final Map<String, String> config) {
        final List<String> keys = Lists.newArrayList();
        for (final String key : config.keySet()) {
            keys.add(key);
        }
        Collections.sort(keys);

        String fileName = StringUtils.EMPTY;
        for (final String key : keys) {
            fileName += key + config.get(key);
        }

        return FilenameUtils.concat(mDirPath, fileName);
    }
}
