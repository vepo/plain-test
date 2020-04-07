package io.vepo.plaintest.runner.executor;

import static io.vepo.plaintest.SuiteAttributes.EXECUTION_PATH;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.IntStream.rangeClosed;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

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
		var results = new ArrayList<Result>();
		rangeClosed(0, suite.lastIndex()).forEachOrdered(index -> {
			if (suite.isStep(index)) {
				var stepResult = executeStep(suite.at(index, Step.class), context);
				logger.debug("Step Executed! results={}", stepResult);
				results.add(stepResult);
				context.addResult(stepResult);
			} else {
				Suite innerSuite = suite.at(index, Suite.class);
				var innerContext = new InnerSuiteContext(context,
						innerSuite.attribute(EXECUTION_PATH, String.class)
								.map(path -> context.getWorkingDirectory().resolve(path).toAbsolutePath())
								.orElse(context.getWorkingDirectory()));
				var suiteResult = executeSuite(innerSuite, innerContext);
				logger.debug("Suite Executed! results={}", suiteResult);
				results.add(suiteResult);
				context.addResult(suiteResult);
			}
		});
		return new Result(suite.name(), start, currentTimeMillis(), results.stream().allMatch(Result::success), "", "",
				results, emptyList());
	}

	private Result executeStep(Step step, Context context) {
		if (stepExecutors.containsKey(step.plugin())) {
			var executor = stepExecutors.get(step.plugin());
			Map<String, Object> missingAttributes = executor.requiredAttribute().entrySet().stream()
					.filter(entry -> !step.attributes().containsKey(entry.getKey()))
					.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
			if (!missingAttributes.isEmpty()) {
				return new Result(step.name(), currentTimeMillis(), currentTimeMillis(), false, "", "", emptyList(),
						asList(new Fail(FailReason.MISSING_ATTRIBUTES, "Missing attributes: ["
								+ missingAttributes.entrySet().stream().map(Entry::getKey).collect(joining(", "))
								+ "]")));
			} else {
				return checkAssertions(step, executor.execute(step, context));
			}
		} else {
			return new Result(step.name(), currentTimeMillis(), currentTimeMillis(), false, "", "", emptyList(),
					asList(new Fail(FailReason.PLUGIN_NOT_FOUND, "Could not find plugin: " + step.plugin())));
		}
	}

	@SuppressWarnings("unchecked")
	private Result checkAssertions(Step step, Result result) {
		List<Assertion<?>> assertions = step.assertions();
		var fails = result.fails();
		assertions.forEach(assertion -> {
			switch (assertion.verb()) {
			case "Contains": {
				if (assertion.value() instanceof String) {
					String value = result.get(assertion.property(), String.class);
					if (!value.contains((String) assertion.value())) {
						fails.add(new Fail(FailReason.ASSERTION,
								assertion.property() + " does not contains " + assertion.value()));
					}
				} else {
					fails.add(new Fail(FailReason.RUNTIME_EXCEPTION,
							assertion.property() + " cannot check contains for numbers. value:" + assertion.value()));
				}
				break;
			}
			case "Equals": {
				if (assertion.value() instanceof String) {
					String value = result.get(assertion.property(), String.class);
					if (value.compareTo((String) assertion.value()) != 0) {
						fails.add(new Fail(FailReason.ASSERTION,
								assertion.property() + " is not equal to " + assertion.value()));
					}
				} else if (assertion.value() instanceof Long) {
					Long value = result.get(assertion.property(), Long.class);
					if (value.longValue() == ((Long) assertion.value()).longValue()) {
						fails.add(new Fail(FailReason.ASSERTION,
								assertion.property() + " is not equal to " + assertion.value()));
					}
				}
				break;
			}
			}
		});
		return new Result(result.name(), result.start(), result.end(), result.success() && fails.isEmpty(),
				result.stdout(), result.stderr(), result.results(), fails);
	}
}
