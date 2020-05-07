package io.vepo.plaintest;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import io.vepo.plaintest.Suite.SuiteBuilder;

public class Step extends NamedSuiteChild {
	public static class StepBuilder {
		private int index;
		private String plugin;
		private String name;
		private Map<String, Object> attributes;
		private List<Assertion<?>> assertions;
		private SuiteBuilder parent;
		private Step instance;

		private StepBuilder() {
			attributes = new HashMap<>();
			assertions = new ArrayList<>();
		}

		public StepBuilder index(int index) {
			this.index = index;
			return this;
		}

		public StepBuilder plugin(String plugin) {
			this.plugin = plugin;
			return this;
		}

		public StepBuilder name(String name) {
			this.name = name;
			return this;
		}

		public StepBuilder attribute(String key, Object value) {
			attributes.put(key, value);
			return this;
		}

		public StepBuilder assertion(Assertion<?> assertion) {
			assertions.add(assertion);
			return this;
		}

		public StepBuilder parent(SuiteBuilder parent) {
			this.parent = parent;
			return this;
		}

		public Step build() {
			if (isNull(instance)) {
				instance = new Step(this);
			}
			return instance;
		}

	}

	public static final StepBuilder builder() {
		return new StepBuilder();
	}

	private final String plugin;
	private final Map<String, Object> attributes;
	private final List<Assertion<?>> assertions;

	private Step(StepBuilder builder) {
		super(builder.index, builder.name, Optional.ofNullable(builder.parent).map(SuiteBuilder::build).orElse(null));
		plugin = builder.plugin;
		attributes = builder.attributes;
		assertions = builder.assertions;
	}

	public String getPlugin() {
		return plugin;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public List<Assertion<?>> getAssertions() {
		return assertions;
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<T> optionalAttribute(String key, Class<T> requiredClass) {
		if (!attributes.containsKey(key)) {
			return Optional.empty();
		}
		Object value = attributes.get(key);
		if (value instanceof PropertyReference) {
			return findOptionalPropertyValue(((PropertyReference) value).getName());
		}
		return Optional.of((T) value);
	}

	@SuppressWarnings("unchecked")
	public <T> T requiredAttribute(String key) {
		if (!attributes.containsKey(key)) {
			throw new IllegalStateException("Missing attribute: " + key);
		}
		Object value = attributes.get(key);
		if (value instanceof PropertyReference) {
			return findRequiredPropertyValue(((PropertyReference) value).getName());
		} else if (value instanceof String) {
			String changedValue = (String) value;
			Matcher propertyMatcher = PropertyReference.regex.matcher(changedValue);
			int start = 0;
			while (propertyMatcher.find(start)) {
				Optional<Object> maybeValue = findOptionalPropertyValue(propertyMatcher.group(1));
				if (maybeValue.isPresent()) {
					String valueAsString = maybeValue.get().toString();
					changedValue = changedValue.replace(propertyMatcher.group(0), valueAsString);
					start = propertyMatcher.start() + valueAsString.length();
				} else {
					start = propertyMatcher.end();
				}
				propertyMatcher = PropertyReference.regex.matcher(changedValue);
			}
			return (T) changedValue;
		}
		return (T) value;
	}

	public boolean hasAttribute(String key) {
		return attributes.containsKey(key);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().appendSuper(super.hashCode()).append(assertions).append(attributes).append(plugin)
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
		Step other = (Step) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(assertions, other.assertions)
				.append(attributes, other.attributes).append(plugin, other.plugin).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("plugin", plugin)
				.append("attributes", attributes).append("assertions", assertions).toString();
	}

}