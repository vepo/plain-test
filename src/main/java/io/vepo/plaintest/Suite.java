package io.vepo.plaintest;

import java.util.List;

public record Suite(String name, List<Suite> subSuites, List<Step> steps) {

	public void addStep(Step step) {
		this.steps.add(step);
	}

	public void addSubSuite(Suite testSuite) {
		this.subSuites.add(testSuite);
	}
}