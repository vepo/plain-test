package io.vepo.plaintest;

import java.util.List;

public record TestSuite(String name, List<TestSuite> subSuites, List<TestStep> steps) {

	public void addStep(TestStep step) {
		this.steps.add(step);
	}

	public void addSubSuite(TestSuite testSuite) {
		this.subSuites.add(testSuite);
	}
}