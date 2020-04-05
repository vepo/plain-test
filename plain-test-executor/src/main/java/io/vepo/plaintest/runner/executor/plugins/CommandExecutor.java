package io.vepo.plaintest.runner.executor.plugins;

import static java.util.Map.entry;
import static java.util.Map.ofEntries;

import java.io.IOException;
import java.util.Map;

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
			Runtime runtime = Runtime.getRuntime();
			logger.info("PWD Before: {}", new String(runtime.exec("pwd").getInputStream().readAllBytes()));
			Process process = runtime.exec(step.attribute("cmd"), new String[] {}, context.getWorkingDirectory());
			int returnCode = process.waitFor();
			logger.info("Return Code: {}", returnCode);
			logger.info("PWD After: {}", new String(runtime.exec("pwd").getInputStream().readAllBytes()));
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
