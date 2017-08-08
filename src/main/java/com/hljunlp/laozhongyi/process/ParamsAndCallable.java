package com.hljunlp.laozhongyi.process;

import java.util.Map;

public class ParamsAndCallable {
    private final Map<String, String> mParams;
    private final ShellProcess mCallable;

    public ParamsAndCallable(final Map<String, String> params, final ShellProcess callable) {
        mParams = params;
        mCallable = callable;
    }

    public Map<String, String> getParams() {
        return mParams;
    }

    public ShellProcess getCallable() {
        return mCallable;
    }
}
