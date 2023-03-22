package com.hljunlp.laozhongyi.process;

import com.google.common.base.Charsets;
import com.hljunlp.laozhongyi.GeneratedFileManager;
import com.hljunlp.laozhongyi.HyperParamResultManager;
import com.hljunlp.laozhongyi.HyperParameterConfig;
import com.hljunlp.laozhongyi.Utils;
import com.hljunlp.laozhongyi.checkpoint.CheckPointManager;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

public class ShellProcess implements Callable<Float> {
    private final Map<String, String> mCopiedHyperParameter;
    private final Set<String> mMultiValueKeys;
    private final String mCmdString;
    private final String mWorkingDir;

    private final CheckPointManager mCheckPointManager;

    public ShellProcess(final Map<String, String> copiedHyperParameter,
                        final Set<String> multiValueKeys, final String cmdString,
                        final String workingDir, CheckPointManager mCheckPointManager) {
        mCopiedHyperParameter = copiedHyperParameter;
        mMultiValueKeys = multiValueKeys;
        mCmdString = cmdString;
        mWorkingDir = workingDir;
        this.mCheckPointManager = mCheckPointManager;
    }

    public float innerCall() {
        final Optional<Float> cachedResult =
                HyperParamResultManager.getResult(mCopiedHyperParameter);
        if (cachedResult.isPresent()) {
            return cachedResult.get();
        }

        final String configFilePath = GeneratedFileManager
                .getHyperParameterConfigFileFullPath(mCopiedHyperParameter, mMultiValueKeys);
        final String newCmdString = mCmdString.replace("{}", configFilePath);
        final HyperParameterConfig config = new HyperParameterConfig(configFilePath);
        config.write(mCopiedHyperParameter);
        final String logFileFullPath = GeneratedFileManager
                .getLogFileFullPath(mCopiedHyperParameter, mMultiValueKeys);
        System.out.println("logFileFullPath:" + logFileFullPath);
        try (OutputStream os = Files.newOutputStream(Paths.get(logFileFullPath))) {
            final DefaultExecutor executor = new DefaultExecutor();
            executor.setStreamHandler(new PumpStreamHandler(os));
            if (mWorkingDir != null) {
                executor.setWorkingDirectory(new File(mWorkingDir));
            }
            System.out.println("begin to execute " + newCmdString);
            final org.apache.commons.exec.CommandLine commandLine =
                    org.apache.commons.exec.CommandLine
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
            final float result = Utils.logResult(log);

            HyperParamResultManager.putResult(mCopiedHyperParameter, result);
            if (mCheckPointManager != null) {
                mCheckPointManager.saveIncrementally();
            }

            return result;
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Float call() {
        try {
            return innerCall();
        } catch (final RuntimeException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
