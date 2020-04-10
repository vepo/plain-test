package io.vepo.plaintest.runner.executor;

import static io.vepo.plaintest.SuiteAttributes.EXECUTION_PATH;
import static java.lang.System.currentTimeMillis;
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

import io.vepo.plaintest.Step;
import io.vepo.plaintest.Suite;
import io.vepo.plaintest.runner.executor.Result.ResultBuilder;
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
		return Result.builder().name(suite.getName()).start(start).end(currentTimeMillis()).results(results)
				.success(results.stream().allMatch(Result::isSuccess)).build();
	}

	private Result executeStep(Step step, Context context) {
		if (stepExecutors.containsKey(step.getPlugin())) {
			StepExecutor executor = stepExecutors.get(step.getPlugin());
			Set<Attribute<?>> missingAttributes = executor.requiredAttribute()
					.filter(entry -> !step.getAttributes().containsKey(entry.key())).collect(toSet());
			if (!missingAttributes.isEmpty()) {
				return Result.builder().name(step.getName()).success(false)
						.fail(new Fail(FailReason.MISSING_ATTRIBUTES,
								"Missing attributes: ["
										+ missingAttributes.stream().map(Attribute::key).collect(joining(", ")) + "]"))
						.build();
			} else {
				return checkAssertions(step, executor.execute(step, context));
			}
		} else {
			return Result.builder().name(step.getName()).success(false)
					.fail(new Fail(FailReason.PLUGIN_NOT_FOUND, "Could not find plugin: " + step.getPlugin())).build();
		}
	}

	private Result checkAssertions(Step step, Result result) {
		ResultBuilder builder = Result.builder(result);
		step.getAssertions().forEach(assertion -> {
			switch (assertion.getVerb()) {
			case "Contains": {
				if (assertion.getValue() instanceof String) {
					String value = result.get(assertion.getProperty(), String.class);
					if (!value.contains((String) assertion.getValue())) {
						builder.success(false).fail(new Fail(FailReason.ASSERTION,
								assertion.getProperty() + " does not contains " + assertion.getValue()));
					}
				} else {
					builder.success(false).fail(new Fail(FailReason.RUNTIME_EXCEPTION, assertion.getProperty()
							+ " cannot check contains for numbers. value:" + assertion.getValue()));
				}
				break;
			}
			case "Equals": {
				if (assertion.getValue() instanceof String) {
					String value = result.get(assertion.getProperty(), String.class);
					if (value.compareTo((String) assertion.getValue()) != 0) {
						builder.success(false).fail(new Fail(FailReason.ASSERTION,
								assertion.getProperty() + " is not equal to " + assertion.getValue()));
					}
				} else if (assertion.getValue() instanceof Long) {
					Long value = result.get(assertion.getProperty(), Long.class);
					if (value.longValue() == ((Long) assertion.getValue()).longValue()) {
						builder.success(false).fail(new Fail(FailReason.ASSERTION,
								assertion.getProperty() + " is not equal to " + assertion.getValue()));
					}
				}
				break;
			}
			}
		});
		return builder.build();
	}
}
