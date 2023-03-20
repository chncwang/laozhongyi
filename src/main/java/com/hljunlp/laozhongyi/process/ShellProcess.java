package com.hljunlp.laozhongyi.process;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Charsets;
import com.hljunlp.laozhongyi.GeneratedFileManager;
import com.hljunlp.laozhongyi.HyperParamResultManager;
import com.hljunlp.laozhongyi.HyperParameterConfig;
import com.hljunlp.laozhongyi.Utils;

public class ShellProcess implements Callable<Pair<Float, Boolean>> {
    private final Map<String, String> mCopiedHyperParameter;
    private final Set<String> mMultiValueKeys;
    private final String mCmdString;
    private final String mWorkingDir;
    private int mTriedTimes = 0;

    public ShellProcess(final Map<String, String> copiedHyperParameter,
            final Set<String> multiValueKeys, final String cmdString,
            final String workingDir) {
        mCopiedHyperParameter = copiedHyperParameter;
        mMultiValueKeys = multiValueKeys;
        mCmdString = cmdString;
        mWorkingDir = workingDir;
    }

    public int getTriedTimes() {
        return mTriedTimes;
    }

    public Pair<Float, Boolean> innerCall() {
        final Optional<Float> cachedResult = HyperParamResultManager
                .getResult(mCopiedHyperParameter, mTriedTimes + 1);
        if (cachedResult.isPresent()) {
            return ImmutablePair.of(cachedResult.get(), false);
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
            final int runtimeMinutes = ProcessManager.runtimeLimitInMinutes(mTriedTimes);
            final ExecuteWatchdog dog = new ExecuteWatchdog(60000L * runtimeMinutes);
            executor.setWatchdog(dog);
            System.out.println("begin to execute " + newCmdString);
            final org.apache.commons.exec.CommandLine commandLine = org.apache.commons.exec.CommandLine
                    .parse(newCmdString);
            boolean isTimeout = false;
            try {
                executor.execute(commandLine);
            } catch (final ExecuteException e) {
                System.out.println(e.getMessage());
                isTimeout = true;
            } catch (final IOException e) {
                throw new IllegalStateException(e);
            }

            final String log = FileUtils.readFileToString(new File(logFileFullPath),
                    Charsets.UTF_8);
            final float result = Utils.logResult(log);

            HyperParamResultManager.putResult(mCopiedHyperParameter, mTriedTimes + 1, result);

            return ImmutablePair.of(result, isTimeout);
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public Pair<Float, Boolean> call() {
        try {
            return innerCall();
        } catch (final RuntimeException e) {
            e.printStackTrace();
            throw e;
        } finally {
            mTriedTimes++;
        }
    }
}
