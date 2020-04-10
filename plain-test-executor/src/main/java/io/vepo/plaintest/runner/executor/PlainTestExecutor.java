package io.vepo.plaintest.runner.executor;

import static io.vepo.plaintest.SuiteAttributes.EXECUTION_PATH;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.rangeClosed;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.plaintest.Assertion;
import io.vepo.plaintest.Step;
import io.vepo.plaintest.Suite;
import io.vepo.plaintest.runner.executor.context.Context;
import io.vepo.plaintest.runner.executor.context.InnerSuiteContext;
import io.vepo.plaintest.runner.executor.context.RootSuiteContext;
import io.vepo.plaintest.runner.executor.plugins.StepExecutor;

public class PlainTestExecutor {
	private static final Logger logger = LoggerFactory.getLogger(PlainTestExecutor.class);
	private Map<String, StepExecutor> stepExecutors;

	public PlainTestExecutor() {
		stepExecutors = new HashMap<>();
		ServiceLoader.load(StepExecutor.class)
				.forEach(stepExecutor -> stepExecutors.put(stepExecutor.name(), stepExecutor));
	}

	public Result execute(Suite suite) {
		return executeSuite(suite, new RootSuiteContext(
				Paths.get(suite.attribute(EXECUTION_PATH, String.class).orElse(".")).toAbsolutePath()));
	}

	private Result executeSuite(Suite suite, Context context) {
		long start = currentTimeMillis();
		List<Result> results = new ArrayList<>();
		rangeClosed(0, suite.lastIndex()).forEachOrdered(index -> {
			if (suite.isStep(index)) {
				Result stepResult = executeStep(suite.at(index, Step.class), context);
				logger.debug("Step Executed! results={}", stepResult);
				results.add(stepResult);
				context.addResult(stepResult);
			} else {
				Suite innerSuite = suite.at(index, Suite.class);
				Context innerContext = new InnerSuiteContext(context,
						innerSuite.attribute(EXECUTION_PATH, String.class)
								.map(path -> context.getWorkingDirectory().resolve(path).toAbsolutePath())
								.orElse(context.getWorkingDirectory()));
				Result suiteResult = executeSuite(innerSuite, innerContext);
				logger.debug("Suite Executed! results={}", suiteResult);
				results.add(suiteResult);
				context.addResult(suiteResult);
			}
		});
		return new Result(suite.getName(), start, currentTimeMillis(), results.stream().allMatch(Result::isSuccess), "",
				"", results, asList());
	}

	private Result executeStep(Step step, Context context) {
		if (stepExecutors.containsKey(step.getPlugin())) {
			StepExecutor executor = stepExecutors.get(step.getPlugin());
			Set<Attribute<?>> missingAttributes = executor.requiredAttribute()
					.filter(entry -> !step.getAttributes().containsKey(entry.key())).collect(toSet());
			if (!missingAttributes.isEmpty()) {
				return new Result(step.getName(), currentTimeMillis(), currentTimeMillis(), false, "", "", emptyList(),
						asList(new Fail(FailReason.MISSING_ATTRIBUTES, "Missing attributes: ["
								+ missingAttributes.stream().map(Attribute::key).collect(joining(", ")) + "]")));
			} else {
				return checkAssertions(step, executor.execute(step, context));
			}
		} else {
			return new Result(step.getName(), currentTimeMillis(), currentTimeMillis(), false, "", "", emptyList(),
					asList(new Fail(FailReason.PLUGIN_NOT_FOUND, "Could not find plugin: " + step.getPlugin())));
		}
	}

	private Result checkAssertions(Step step, Result result) {
		List<Assertion<?>> assertions = step.getAssertions();
		List<Fail> fails = result.getFails();
		assertions.forEach(assertion -> {
			switch (assertion.getVerb()) {
			case "Contains": {
				if (assertion.getValue() instanceof String) {
					String value = result.get(assertion.getProperty(), String.class);
					if (!value.contains((String) assertion.getValue())) {
						fails.add(new Fail(FailReason.ASSERTION,
								assertion.getProperty() + " does not contains " + assertion.getValue()));
					}
				} else {
					fails.add(new Fail(FailReason.RUNTIME_EXCEPTION, assertion.getProperty()
							+ " cannot check contains for numbers. value:" + assertion.getValue()));
				}
				break;
			}
			case "Equals": {
				if (assertion.getValue() instanceof String) {
					String value = result.get(assertion.getProperty(), String.class);
					if (value.compareTo((String) assertion.getValue()) != 0) {
						fails.add(new Fail(FailReason.ASSERTION,
								assertion.getProperty() + " is not equal to " + assertion.getValue()));
					}
				} else if (assertion.getValue() instanceof Long) {
					Long value = result.get(assertion.getProperty(), Long.class);
					if (value.longValue() == ((Long) assertion.getValue()).longValue()) {
						fails.add(new Fail(FailReason.ASSERTION,
								assertion.getProperty() + " is not equal to " + assertion.getValue()));
					}
				}
				break;
			}
			}
		});
		return new Result(result.getName(), result.getStart(), result.getEnd(), result.isSuccess() && fails.isEmpty(),
				result.getStdout(), result.getStderr(), result.getResults(), fails);
	}
}
