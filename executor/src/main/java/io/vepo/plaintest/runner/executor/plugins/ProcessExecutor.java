package io.vepo.plaintest.runner.executor.plugins;

import static io.vepo.plaintest.runner.executor.Attribute.createAttribute;
import static io.vepo.plaintest.runner.executor.FailReason.FAILED;
import static io.vepo.plaintest.runner.executor.FailReason.TIMED_OUT;
import static io.vepo.plaintest.runner.utils.Os.OS.WINDOWS;
import static io.vepo.plaintest.runner.utils.Timeout.executeWithTimeout;
import static java.lang.System.currentTimeMillis;
import static java.util.regex.Matcher.quoteReplacement;
import static java.util.regex.Pattern.quote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.plaintest.Step;
import io.vepo.plaintest.runner.executor.Attribute;
import io.vepo.plaintest.runner.executor.Fail;
import io.vepo.plaintest.runner.executor.Result;
import io.vepo.plaintest.runner.executor.Result.ResultBuilder;
import io.vepo.plaintest.runner.executor.context.Context;
import io.vepo.plaintest.runner.utils.Os;

public class ProcessExecutor implements StepExecutor {
    private static final Logger logger = LoggerFactory.getLogger(ProcessExecutor.class);
    public static final String COMMAND_EXECUTOR_PLUGIN_NAME = "Process";

    private static final String CMD_ATTRIBUTE_KEY = "cmd";
    private static final String TIMEOUT_ATTRIBUTE_KEY = "timeout";
    private static final String ADJUST_FILE_SEPARATOR_ATTRIBUTE_KEY = "adjustFileSeparator";

    private static final String PROPERTY_STDERR_KEY = "stderr";
    private static final String PROPERTY_STDOUT_KEY = "stdout";
    private static final String PROPERTY_EXIT_VALUE_KEY = "exitValue";

    @Override
    public String name() {
        return COMMAND_EXECUTOR_PLUGIN_NAME;
    }

    @Override
    public Result execute(Step step, Context context) {
        long start = currentTimeMillis();
        ResultBuilder resultBuilder = Result.builder().name(step.getName()).start(start);
        try {
            String executionCommand = step.requiredAttribute(CMD_ATTRIBUTE_KEY, String.class);
            Optional<Boolean> maybeAdjustFileSeparator = step.optionalAttribute(ADJUST_FILE_SEPARATOR_ATTRIBUTE_KEY,
                    Boolean.class);
            if (maybeAdjustFileSeparator.isPresent() && maybeAdjustFileSeparator.get().booleanValue()) {
                if (Os.getOS() == WINDOWS) {
                    executionCommand = executionCommand.replaceAll(quote("/"), quoteReplacement("\\"));
                } else {
                    executionCommand = executionCommand.replaceAll(quote("\\"), quoteReplacement("/"));
                }
            }
            String[] cmd = Os.getOS() == WINDOWS ? new String[] {
                "cmd.exe",
                "/c",
                executionCommand }
                    : new String[] {
                        "/bin/sh",
                        "-c",
                        executionCommand };
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(context.getWorkingDirectory().toFile());
            logger.info("Executing command: step={} context={}", step, context);

            Process p = pb.start();

            Optional<Long> maybeTimeout = step.optionalAttribute(TIMEOUT_ATTRIBUTE_KEY, Long.class);
            Optional<Integer> returnValue = executeWithTimeout(() -> {
                logger.info("Waiting for Thread...");
                resultBuilder.property(PROPERTY_STDOUT_KEY, captureOutput(p.getInputStream()))
                        .property(PROPERTY_STDERR_KEY, captureOutput(p.getErrorStream()));
                return p.waitFor();
            }, maybeTimeout);

            boolean success;
            int exitValue;
            if (returnValue.isPresent()) {
                exitValue = returnValue.get();
                if (exitValue != 0) {
                    resultBuilder.fail(new Fail(FAILED, "Exit code: " + exitValue));
                    success = false;
                } else {
                    success = true;
                }
            } else {
                resultBuilder.fail(new Fail(TIMED_OUT,
                        String.format("Execution exceeds timeout! timeout=%dms", maybeTimeout.orElse(-1L))));
                success = false;
                exitValue = -1;
            }
            return resultBuilder.end(currentTimeMillis()).success(success).property(PROPERTY_EXIT_VALUE_KEY, exitValue)
                    .build();
        } catch (IOException e) {
            logger.warn("Execution error!", e);
            return resultBuilder.end(currentTimeMillis()).success(false).fail(new Fail(FAILED, e.getMessage())).build();
        }
    }

    private String captureOutput(InputStream inputStream) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));) {
            StringJoiner sj = new StringJoiner(System.getProperty("line.separator"));
            reader.lines().iterator().forEachRemaining(sj::add);
            return sj.toString();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<Attribute<?>> requiredAttribute() {
        return Stream.of(createAttribute(CMD_ATTRIBUTE_KEY, String.class, true),
                createAttribute(TIMEOUT_ATTRIBUTE_KEY, Long.class, false),
                createAttribute(ADJUST_FILE_SEPARATOR_ATTRIBUTE_KEY, Boolean.class, false));
    }

}
