package io.vepo.plaintest.runner.executor;

import static io.vepo.plaintest.SuiteAttributes.EXECUTION_PATH;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.IntStream.rangeClosed;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
						asList(new Fail(FailReason.MISSING_ATTRIBUTES, "Missing attributes: " + missingAttributes)));
			} else {
				return executor.execute(step, context);
			}
		} else {
			return new Result(step.name(), currentTimeMillis(), currentTimeMillis(), false, "", "", emptyList(),
					asList(new Fail(FailReason.PLUGIN_NOT_FOUND, "Could not find plugin: " + step.plugin())));
		}
	}
}
