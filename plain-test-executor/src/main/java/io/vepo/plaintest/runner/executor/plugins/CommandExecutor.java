package io.vepo.plaintest.runner.executor.plugins;

import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptyList;
import static java.util.Map.entry;
import static java.util.Map.ofEntries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.plaintest.Step;
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
			ProcessBuilder pb;
			if (Os.getOS() == OS.WINDOWS) {
				pb = new ProcessBuilder("cmd.exe", "/c", step.attribute("cmd"));
			} else {
				pb = new ProcessBuilder(step.attribute("cmd"));
			}
			pb.directory(context.getWorkingDirectory().toFile());
			pb.environment().putAll(System.getenv());
			logger.info("Executing command: step={} context={}", step, context);
			var p = pb.start();
			var stdout = captureOutpu(p.getInputStream());
			var stderr = captureOutpu(p.getErrorStream());
			p.waitFor();

			var fails = new ArrayList<Fail>();

			if (p.exitValue() != 0) {
				fails.add(new Fail(FailReason.FAILED, "Exit code: " + p.exitValue()));
			}
			return new Result(step.name(), start, currentTimeMillis(), fails.isEmpty(), stdout, stderr, emptyList(),
					fails);
		} catch (IOException e) {
			logger.warn("Execution error!", e);
			return new Result(step.name(), start, currentTimeMillis(), false, "", "", emptyList(),
					Arrays.asList(new Fail(FailReason.FAILED, e.getMessage())));
		} catch (InterruptedException e) {
			throw new RuntimeException("Execution Stopped!");
		}
	}

	private String captureOutpu(InputStream inputStream) throws IOException {
		try (var reader = new BufferedReader(new InputStreamReader(inputStream));) {
			var sj = new StringJoiner(System.getProperty("line.separator"));
			reader.lines().iterator().forEachRemaining(sj::add);
			return sj.toString();
		}
	}

	@Override
	public Map<String, Class<?>> requiredAttribute() {
		return ofEntries(entry("cmd", String.class), entry("timeout", Long.class));
	}

}
