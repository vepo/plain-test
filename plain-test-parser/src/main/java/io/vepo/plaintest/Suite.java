package io.vepo.plaintest;

import static io.vepo.plaintest.SuiteAttributes.EXECUTION_PATH;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

public class Suite {
	private int index;
	private String name;
	private List<Suite> suites;
	private List<Step> steps;
	private Map<SuiteAttributes, Object> attributes;

	public Suite(int index, String name, List<Suite> suites, List<Step> steps,
			Map<SuiteAttributes, Object> attributes) {
		this.index = index;
		this.name = name;
		this.suites = suites;
		this.steps = steps;
		this.attributes = attributes;
	}

	public Suite() {
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Suite> getSuites() {
		return suites;
	}

	public void setSuites(List<Suite> suites) {
		this.suites = suites;
	}

	public List<Step> getSteps() {
		return steps;
	}

	public void setSteps(List<Step> steps) {
		this.steps = steps;
	}

	public Map<SuiteAttributes, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<SuiteAttributes, Object> attributes) {
		this.attributes = attributes;
	}

	public void addStep(Step step) {
		this.steps.add(step);
	}

	public void addSuite(Suite suite) {
		this.suites.add(suite);
	}

	public int lastIndex() {
		return IntStream.concat(suites.stream().mapToInt(Suite::getIndex), steps.stream().mapToInt(Step::getIndex))
				.max().orElse(-1);
	}

	public boolean isStep(int position) {
		return this.steps.stream().filter(step -> step.getIndex() == position).count() == 1L;
	}

	public boolean isSuite(int position) {
		return this.suites.stream().filter(step -> step.getIndex() == position).count() == 1L;
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
			return (T) this.steps.stream().filter(step -> step.getIndex() == index).findFirst()
					.orElseThrow(() -> new RuntimeException("Could not find a Step on this position! index=" + index));
		} else if (requiredClass == Suite.class) {
			return (T) this.suites.stream().filter(suite -> suite.getIndex() == index).findFirst()
					.orElseThrow(() -> new RuntimeException("Could not find a Suite on this position! index=" + index));
		} else {
			throw new RuntimeException("Unexpected type: " + requiredClass);
		}
	}

	public void setExecDirectory(String execDirectory) {
		this.attributes.put(EXECUTION_PATH, execDirectory);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + index;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((steps == null) ? 0 : steps.hashCode());
		result = prime * result + ((suites == null) ? 0 : suites.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Suite other = (Suite) obj;
		if (attributes == null) {
			if (other.attributes != null)
				return false;
		} else if (!attributes.equals(other.attributes))
			return false;
		if (index != other.index)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (steps == null) {
			if (other.steps != null)
				return false;
		} else if (!steps.equals(other.steps))
			return false;
		if (suites == null) {
			if (other.suites != null)
				return false;
		} else if (!suites.equals(other.suites))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Suite [index=" + index + ", name=" + name + ", suites=" + suites + ", steps=" + steps + ", attributes="
				+ attributes + "]";
	}

}