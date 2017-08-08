package com.hljunlp.laozhongyi;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Maps;

public class Utils {
    public static Map<String, String> modifiedNewMap(final Map<String, String> map,
            final String key, final String value) {
        final Map<String, String> copied = Maps.newTreeMap();
        for (final Map.Entry<String, String> entry : map.entrySet()) {
            copied.put(entry.getKey(), entry.getValue());
        }
        copied.put(key, value);
        return copied;
    }

    public static float logResult(final String log) {
        final Pattern pattern = Pattern.compile("laozhongyi_([\\d\\.]+)");
        final Matcher matcher = pattern.matcher(log);
        float result = 0.0f;
        while (matcher.find()) {
            final String string = matcher.group(1);
            result = Float.valueOf(string);
        }
        return result;
    }
}
