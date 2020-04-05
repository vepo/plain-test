package io.vepo.plaintest;

import java.util.List;
import java.util.stream.IntStream;

public record Suite(int index, String name, List<Suite> subSuites, List<Step> steps) {

	public void addStep(Step step) {
		this.steps.add(step);
	}

	public void addSubSuite(Suite testSuite) {
		this.subSuites.add(testSuite);
	}

	public int lastIndex() {
		return IntStream.concat(subSuites.stream().mapToInt(Suite::index), steps.stream().mapToInt(Step::index)).max()
				.orElse(-1);
	}
}