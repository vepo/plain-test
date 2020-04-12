package io.vepo.plaintest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Step {
	private int index;
	private String plugin;
	private String name;
	private Map<String, Object> attributes;
	private List<Assertion<?>> assertions;

	public Step(int index, String plugin, String name, Map<String, Object> attributes, List<Assertion<?>> assertions) {
		this.index = index;
		this.plugin = plugin;
		this.name = name;
		this.attributes = attributes;
		this.assertions = assertions;
	}

	public Step() {
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getPlugin() {
		return plugin;
	}

	public void setPlugin(String plugin) {
		this.plugin = plugin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, Object> getAttributes() {
		return attributes;
	}

	public void setAttributes(Map<String, Object> attributes) {
		this.attributes = attributes;
	}

	public List<Assertion<?>> getAssertions() {
		return assertions;
	}

	public void setAssertions(List<Assertion<?>> assertions) {
		this.assertions = assertions;
	}

	public void addStringAttribute(String key, String value) {
		attributes.put(key, value);
	}

	public void addNumberAttribute(String key, Long value) {
		attributes.put(key, value);
	}

	public <T> void addAssertion(Assertion<T> assertion) {
		assertions.add(assertion);
	}

	@SuppressWarnings("unchecked")
	public <T> Optional<T> optionalAttribute(String key, Class<T> requiredClass) {
		if (!attributes.containsKey(key)) {
			return Optional.empty();
		}
		return Optional.of((T) attributes.get(key));
	}

	@SuppressWarnings("unchecked")
	public <T> T requiredAttribute(String key) {
		if (!attributes.containsKey(key)) {
			throw new IllegalStateException("Missing attribute: " + key);
		}
		return (T) attributes.get(key);
	}

	public boolean hasAttribute(String key) {
		return attributes.containsKey(key);
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(assertions).append(attributes).append(index).append(name).append(plugin)
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
		return new EqualsBuilder().append(assertions, other.assertions).append(attributes, other.attributes)
				.append(index, other.index).append(name, other.name).append(plugin, other.plugin).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("index", index).append("name", name)
				.append("plugin", plugin).append("attributes", attributes).append("assertions", assertions).toString();
	}

}