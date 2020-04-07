package io.vepo.plaintest;

import static io.vepo.plaintest.SuiteAttributes.EXECUTION_PATH;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public record Suite(int index, String name, List<Suite> suites, List<Step> steps,
		Map<SuiteAttributes, Object> attributes) {

	public void addStep(Step step) {
		this.steps.add(step);
	}

	public void addSuite(Suite suite) {
		this.suites.add(suite);
	}

	public int lastIndex() {
		return IntStream.concat(suites.stream().mapToInt(Suite::index), steps.stream().mapToInt(Step::index)).max()
				.orElse(-1);
	}

	public boolean isStep(int position) {
		return this.steps.stream().filter(step -> step.index() == position).count() == 1L;
	}

	public boolean isSuite(int position) {
		return this.suites.stream().filter(step -> step.index() == position).count() == 1L;
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<T> attribute(SuiteAttributes key) {
		if (this.attributes.containsKey(key)) {
			return Optional.of((T) this.attributes.get(key));
		} else {
			return Optional.empty();
		}
	}

	public <T> Optional<T> attribute(SuiteAttributes key, Class<T> requiredClass) {
		return attribute(key);
	}

	@SuppressWarnings("unchecked")
	public <T> T at(int index, Class<T> requiredClass) {
		if (requiredClass == Step.class) {
			return (T) this.steps.stream().filter(step -> step.index() == index).findFirst()
					.orElseThrow(() -> new RuntimeException("Could not find a Step on this position! index=" + index));
		} else if (requiredClass == Suite.class) {
			return (T) this.suites.stream().filter(suite -> suite.index() == index).findFirst()
					.orElseThrow(() -> new RuntimeException("Could not find a Suite on this position! index=" + index));
		} else {
			throw new RuntimeException("Unexpected type: " + requiredClass);
		}
	}

	public void setExecDirectory(String execDirectory) {
		this.attributes.put(EXECUTION_PATH, execDirectory);
	}
}