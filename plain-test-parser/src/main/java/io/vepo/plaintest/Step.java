package io.vepo.plaintest;

import java.util.List;
import java.util.Map;

public record Step(int index, String plugin, String name, Map<String, Object> attributes,
		List<Assertion<?>> assertions) {

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
}