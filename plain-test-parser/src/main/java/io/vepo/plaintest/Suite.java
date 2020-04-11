package io.vepo.plaintest;

import static io.vepo.plaintest.SuiteAttributes.EXECUTION_PATH;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

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
		steps.add(step);
	}

	public void addSuite(Suite suite) {
		suites.add(suite);
	}

	public int lastIndex() {
		return IntStream.concat(suites.stream().mapToInt(Suite::getIndex), steps.stream().mapToInt(Step::getIndex))
				.max().orElse(-1);
	}

	public boolean isStep(int position) {
		return steps.stream().filter(step -> step.getIndex() == position).count() == 1L;
	}

	public boolean isSuite(int position) {
		return suites.stream().filter(step -> step.getIndex() == position).count() == 1L;
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<T> attribute(SuiteAttributes key) {
		if (attributes.containsKey(key)) {
			return Optional.of((T) attributes.get(key));
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
			return (T) steps.stream().filter(step -> step.getIndex() == index).findFirst()
					.orElseThrow(() -> new RuntimeException("Could not find a Step on this position! index=" + index));
		} else if (requiredClass == Suite.class) {
			return (T) suites.stream().filter(suite -> suite.getIndex() == index).findFirst()
					.orElseThrow(() -> new RuntimeException("Could not find a Suite on this position! index=" + index));
		} else {
			throw new IllegalStateException("Unexpected type: " + requiredClass);
		}
	}

	public void setExecDirectory(String execDirectory) {
		attributes.put(EXECUTION_PATH, execDirectory);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(index).append(name).append(attributes).append(steps).append(suites)
				.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Suite other = (Suite) obj;
		return new EqualsBuilder().append(index, other.index).append(name, other.name)
				.append(attributes, other.attributes).append(steps, other.steps).append(suites, other.suites)
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("index", index).append("name", name)
				.append("attributes", attributes).append("steps", steps).append("suites", suites).toString();
	}

}