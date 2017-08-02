package com.hljunlp.laozhongyi;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class HyperParameterScopeItem {
    private final String mKey;
    private final List<String> mValues;

    public HyperParameterScopeItem(final String key, final List<String> values) {
        super();
        this.mKey = key;
        this.mValues = values;
    }

    public String getKey() {
        return mKey;
    }

    public List<String> getValues() {
        return mValues;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
