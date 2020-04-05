package io.vepo.plaintest.runner.executor;

import static java.util.stream.IntStream.rangeClosed;

import io.vepo.plaintest.Step;
import io.vepo.plaintest.Suite;
import io.vepo.plaintest.runner.executor.context.InnerSuiteContext;
import io.vepo.plaintest.runner.executor.context.RootSuiteContext;
import io.vepo.plaintest.runner.executor.context.TestContext;

public class PlainTestExecutor {

	public TestResults execute(Suite suite) {
		return executeSuite(suite, new RootSuiteContext());
	}

	private TestResults executeSuite(Suite suite, TestContext context) {
		var results = new TestResults();
		rangeClosed(0, suite.lastIndex()).forEachOrdered(index -> {
			if (suite.isStep(index)) {
				executeStep(suite.at(index, Step.class), context);
			} else {
				executeSuite(suite.at(index, Suite.class), new InnerSuiteContext(context));
			}
		});
		return results;
	}

	private void executeStep(Step step, TestContext context) {

	}
}
