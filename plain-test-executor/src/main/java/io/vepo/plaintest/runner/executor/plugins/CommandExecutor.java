package io.vepo.plaintest.runner.executor.plugins;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.StringJoiner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.plaintest.Step;
import io.vepo.plaintest.runner.executor.ExecutionStatus;
import io.vepo.plaintest.runner.executor.Result;
import io.vepo.plaintest.runner.executor.StepResult;
import io.vepo.plaintest.runner.executor.context.Context;

public class CommandExecutor implements StepExecutor {
	private static final Logger logger = LoggerFactory.getLogger(CommandExecutor.class);
	public static final String COMMAND_EXECUTOR_PLUGIN_NAME = "CMD";

	@Override
	public String name() {
		return COMMAND_EXECUTOR_PLUGIN_NAME;
	}

	@Override
	public Result execute(Step step, Context context) {
		try {
			var pb = new ProcessBuilder("pwd", step.attribute("cmd"), "pwd");
			logger.info("CMDs: {}", pb.command());
			var p = pb.start();
			try (var reader = new BufferedReader(new InputStreamReader(p.getInputStream()));) {

				var sj = new StringJoiner(System.getProperty("line.separator"));
				reader.lines().iterator().forEachRemaining(sj::add);
				var result = sj.toString();
				logger.info("STDOUT: {}", result);
				logger.info("STDOUT: {}", pb.directory());
			}
			p.waitFor();

			return new StepResult(step.name(), ExecutionStatus.EXECUTED, "");
		} catch (IOException e) {
			return new StepResult(step.name(), ExecutionStatus.FAILED, e.getMessage());
		} catch (InterruptedException e) {
			throw new RuntimeException("Execution Stopped!");
		}
	}

	@Override
	public Map<String, Class<?>> requiredAttribute() {
		return ofEntries(entry("cmd", String.class), entry("timeout", Long.class));
	}

}
