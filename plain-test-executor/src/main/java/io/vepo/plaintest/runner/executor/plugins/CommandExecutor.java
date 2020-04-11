package io.vepo.plaintest.runner.executor.plugins;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.currentThread;
import static java.util.concurrent.Executors.newSingleThreadExecutor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.plaintest.Step;
import io.vepo.plaintest.runner.executor.Attribute;
import io.vepo.plaintest.runner.executor.Fail;
import io.vepo.plaintest.runner.executor.FailReason;
import io.vepo.plaintest.runner.executor.Result;
import io.vepo.plaintest.runner.executor.Result.ResultBuilder;
import io.vepo.plaintest.runner.executor.context.Context;
import io.vepo.plaintest.runner.utils.Os;
import io.vepo.plaintest.runner.utils.Os.OS;

public class CommandExecutor implements StepExecutor {
	private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);
	public static final String COMMAND_EXECUTOR_PLUGIN_NAME = "CMD";

	private static final String TIMEOUT_ATTRIBUTE_KEY = "timeout";

	private static final String PROPERTY_STDERR_KEY = "stderr";
	private static final String PROPERTY_STDOUT_KEY = "stdout";
	private static final String PROPERTY_EXIT_VALUE_KEY = "exitValue";

	private static final long MAX_ALLOWED_TIME = 250;

	@Override
	public String name() {
		return COMMAND_EXECUTOR_PLUGIN_NAME;
	}

	@Override
	public Result execute(Step step, Context context) {
		long start = currentTimeMillis();
		ResultBuilder resultBuilder = Result.builder().name(step.getName()).start(start);
		try {

			String[] cmd = Os.getOS() == OS.WINDOWS ? new String[] { "cmd.exe", "/c", step.attribute("cmd") }
					: new String[] { "/bin/sh", "-c", step.attribute("cmd") };
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.directory(context.getWorkingDirectory().toFile());
			logger.info("Executing command: step={} context={}", step, context);

			Process p = pb.start();

			AtomicInteger exitValue = new AtomicInteger(-1);
			Future<?> execution = newSingleThreadExecutor().submit(() -> {
				try {
					logger.info("Waiting for Thread...");
					resultBuilder.property(PROPERTY_STDOUT_KEY, captureOutput(p.getInputStream()))
							.property(PROPERTY_STDERR_KEY, captureOutput(p.getErrorStream()));
					exitValue.set(p.waitFor());
				} catch (IOException e) {
					logger.warn("Thread interrupted!", e);
					exitValue.set(Integer.MIN_VALUE);
				} catch (InterruptedException ie) {
					Thread.currentThread().interrupt();
				}
				return null;
			});
			logger.warn("New process waiting in background...");
			if (step.hasAttribute(TIMEOUT_ATTRIBUTE_KEY)) {
				long timeout = step.attribute(TIMEOUT_ATTRIBUTE_KEY);
				try {
					execution.get(timeout + MAX_ALLOWED_TIME, TimeUnit.MILLISECONDS);
				} catch (ExecutionException e) {
					logger.warn("Execution error!", e);
					resultBuilder.fail(new Fail(FailReason.RUNTIME_EXCEPTION, e.getMessage()));
				}
			} else {
				execution.get();
			}

			boolean success = true;
			if (exitValue.get() != 0) {
				resultBuilder.fail(new Fail(FailReason.FAILED, "Exit code: " + exitValue));
				success = false;
			}
			return resultBuilder.end(currentTimeMillis()).success(success).property(PROPERTY_EXIT_VALUE_KEY, exitValue)
					.build();
		} catch (ExecutionException | TimeoutException | IOException e) {
			logger.warn("Execution error!", e);
			return resultBuilder.end(currentTimeMillis()).success(false)
					.fail(new Fail(FailReason.FAILED, e.getMessage())).build();
		} catch (InterruptedException e) {
			currentThread().interrupt();
			return null;
		}
	}

	private String captureOutput(InputStream inputStream) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));) {
			StringJoiner sj = new StringJoiner(System.getProperty("line.separator"));
			reader.lines().iterator().forEachRemaining(sj::add);
			return sj.toString();
		}
	}

	@Override
	public Stream<Attribute<?>> requiredAttribute() {
		return Stream.of(new Attribute<>("cmd", String.class, true),
				new Attribute<>(TIMEOUT_ATTRIBUTE_KEY, Long.class, false));
	}

}
