package io.vepo.plaintest.runner.executor;

import static java.util.stream.IntStream.rangeClosed;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import io.vepo.plaintest.Step;
import io.vepo.plaintest.Suite;
import io.vepo.plaintest.runner.executor.context.Context;
import io.vepo.plaintest.runner.executor.context.InnerSuiteContext;
import io.vepo.plaintest.runner.executor.context.RootSuiteContext;
import io.vepo.plaintest.runner.executor.plugins.StepExecutor;

public class PlainTestExecutor {
	private Map<String, StepExecutor> stepExecutors;

	public PlainTestExecutor() {
		stepExecutors = new HashMap<>();
		ServiceLoader.load(StepExecutor.class)
				.forEach(stepExecutor -> stepExecutors.put(stepExecutor.name(), stepExecutor));
	}

	public Result execute(Suite suite) {
		return executeSuite(suite, new RootSuiteContext());
	}

	private Result executeSuite(Suite suite, Context context) {
		var results = new Result();
		rangeClosed(0, suite.lastIndex()).forEachOrdered(index -> {
			if (suite.isStep(index)) {
				context.addResult(executeStep(suite.at(index, Step.class), context));
			} else {
				executeSuite(suite.at(index, Suite.class), new InnerSuiteContext(context));
			}
		});
		return results;
	}

	private Result executeStep(Step step, Context context) {
		if (stepExecutors.containsKey(step.plugin())) {
			var executor = stepExecutors.get(step.plugin());
			Map<String, Object> missingAttributes = executor.requiredAttribute().entrySet().stream()
					.filter(entry -> !step.attributes().containsKey(entry.getKey()))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			if (!missingAttributes.isEmpty()) {
				return new StepResult(step.name(), ExecutionStatus.MISSING_ATTRIBUTES,
						"Missing attributes: " + missingAttributes);
			} else {
				return executor.execute(step, context);
			}
		} else {
			return new StepResult(step.name(), ExecutionStatus.PLUGIN_NOT_FOUND,
					"Could not find plugin: " + step.plugin());
		}
	}
}
