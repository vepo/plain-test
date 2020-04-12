package io.vepo.plaintest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Suite {
	public static final class SuiteBuilder {
		private int index;
		private String name;
		private List<Suite> suites;
		private List<Step> steps;
		private Map<SuiteAttributes, Object> attributes;

		private SuiteBuilder() {
			attributes = new HashMap<>();
			suites = new ArrayList<>();
			steps = new ArrayList<>();
		}

		public SuiteBuilder index(int index) {
			this.index = index;
			return this;
		}

		public SuiteBuilder name(String name) {
			this.name = name;
			return this;
		}

		public SuiteBuilder suite(Suite suite) {
			suites.add(suite);
			return this;
		}

		public SuiteBuilder step(Step step) {
			steps.add(step);
			return this;
		}

		public SuiteBuilder attribute(SuiteAttributes key, Object value) {
			attributes.put(key, value);
			return this;
		}

		public Suite build() {
			return new Suite(this);
		}

		public int nextIndex() {
			return IntStream.concat(suites.stream().mapToInt(Suite::getIndex), steps.stream().mapToInt(Step::getIndex))
					.max().orElse(-1) + 1;

		}
	}

	public static final SuiteBuilder builder() {
		return new SuiteBuilder();
	}

	private final int index;
	private final String name;
	private final List<Suite> suites;
	private final List<Step> steps;
	private final Map<SuiteAttributes, Object> attributes;

	private Suite(SuiteBuilder builder) {
		index = builder.index;
		name = builder.name;
		suites = builder.suites;
		steps = builder.steps;
		attributes = builder.attributes;
	}

	public int getIndex() {
		return index;
	}

	public String getName() {
		return name;
	}

	public List<Suite> getSuites() {
		return suites;
	}

	public List<Step> getSteps() {
		return steps;
	}

	public Map<SuiteAttributes, Object> getAttributes() {
		return attributes;
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
		if (attributes.containsKey(key)) {
			Object value = attributes.get(key);
			if (value.getClass().isAssignableFrom(requiredClass)) {
				return Optional.of((T) value);
			} else {
				throw new IllegalStateException("Invalid type!");
			}
		} else {
			return Optional.empty();
		}
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

	public void forEachOrdered(Consumer<Suite> suiteConsumer, Consumer<Step> stepConsumer) {
		AtomicInteger currentIndex = new AtomicInteger(0);
		List<Step> remainingSteps = new LinkedList<>(steps);
		List<Suite> remainingSuites = new LinkedList<>(suites);
		while (!remainingSteps.isEmpty() || !remainingSuites.isEmpty()) {
			remainingSteps.removeIf(step -> {
				if (step.getIndex() == currentIndex.get()) {
					stepConsumer.accept(step);
					currentIndex.incrementAndGet();
					return true;
				} else {
					return false;
				}
			});

			remainingSuites.removeIf(step -> {
				if (step.getIndex() == currentIndex.get()) {
					suiteConsumer.accept(step);
					currentIndex.incrementAndGet();
					return true;
				} else {
					return false;
				}
			});
		}
	}
}