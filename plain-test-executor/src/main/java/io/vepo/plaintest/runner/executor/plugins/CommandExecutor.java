package io.vepo.plaintest.runner.executor.plugins;

import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.plaintest.Step;
import io.vepo.plaintest.runner.executor.Attribute;
import io.vepo.plaintest.runner.executor.Fail;
import io.vepo.plaintest.runner.executor.FailReason;
import io.vepo.plaintest.runner.executor.Result;
import io.vepo.plaintest.runner.executor.context.Context;
import io.vepo.plaintest.runner.utils.Os;
import io.vepo.plaintest.runner.utils.Os.OS;

public class CommandExecutor implements StepExecutor {
	private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);
	public static final String COMMAND_EXECUTOR_PLUGIN_NAME = "CMD";

	@Override
	public String name() {
		return COMMAND_EXECUTOR_PLUGIN_NAME;
	}

	@Override
	public Result execute(Step step, Context context) {
		long start = currentTimeMillis();
		try {
			String[] cmd = Os.getOS() == OS.WINDOWS ? new String[] { "cmd.exe", "/c", step.attribute("cmd") }
					: new String[] { step.attribute("cmd") };
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.directory(context.getWorkingDirectory().toFile());
			logger.info("Executing command: step={} context={}", step, context);
			Process p = pb.start();
			String stdout = captureOutpu(p.getInputStream());
			String stderr = captureOutpu(p.getErrorStream());
			p.waitFor();

			List<Fail> fails = new ArrayList<>();

			if (p.exitValue() != 0) {
				fails.add(new Fail(FailReason.FAILED, "Exit code: " + p.exitValue()));
			}
			return new Result(step.getName(), start, currentTimeMillis(), fails.isEmpty(), stdout, stderr, emptyList(),
					fails);
		} catch (IOException e) {
			logger.warn("Execution error!", e);
			return new Result(step.getName(), start, currentTimeMillis(), false, "", "", emptyList(),
					asList(new Fail(FailReason.FAILED, e.getMessage())));
		} catch (InterruptedException e) {
			throw new RuntimeException("Execution Stopped!");
		}
	}

	private String captureOutpu(InputStream inputStream) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));) {
			StringJoiner sj = new StringJoiner(System.getProperty("line.separator"));
			reader.lines().iterator().forEachRemaining(sj::add);
			return sj.toString();
		}
	}

	@Override
	public Stream<Attribute<?>> requiredAttribute() {
		return Stream.of(new Attribute<>("cmd", String.class), new Attribute<>("timeout", Long.class));
	}

}
