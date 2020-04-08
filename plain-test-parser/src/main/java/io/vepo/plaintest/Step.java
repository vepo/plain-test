package io.vepo.plaintest;

import java.util.List;
import java.util.Map;

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
		this.attributes.put(key, value);
	}

	public void addNumberAttribute(String key, Long value) {
		this.attributes.put(key, value);
	}

	public <T> void addAssertion(Assertion<T> assertion) {
		this.assertions.add(assertion);
	}

	public String attribute(String key) {
		if (!this.attributes.containsKey(key)) {
			throw new IllegalStateException("Missing attribute: " + key);
		}
		return (String) this.attributes.get(key);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((assertions == null) ? 0 : assertions.hashCode());
		result = prime * result + ((attributes == null) ? 0 : attributes.hashCode());
		result = prime * result + index;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((plugin == null) ? 0 : plugin.hashCode());
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
		Step other = (Step) obj;
		if (assertions == null) {
			if (other.assertions != null)
				return false;
		} else if (!assertions.equals(other.assertions))
			return false;
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
		if (plugin == null) {
			if (other.plugin != null)
				return false;
		} else if (!plugin.equals(other.plugin))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Step [index=" + index + ", plugin=" + plugin + ", name=" + name + ", attributes=" + attributes
				+ ", assertions=" + assertions + "]";
	}

}