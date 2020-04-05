package io.vepo.plaintest.runner.executor.plugins;

import java.util.Map;

import io.vepo.plaintest.Step;
import io.vepo.plaintest.runner.executor.Result;
import io.vepo.plaintest.runner.executor.context.Context;

public interface StepExecutor {
	public String name();
	public Result execute(Step step, Context context);
	public Map<String, Class<?>> requiredAttribute();
}
