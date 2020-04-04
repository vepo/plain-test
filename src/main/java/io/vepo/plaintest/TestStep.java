package io.vepo.plaintest;

import java.util.Map;

public record TestStep(String plugin, String name, Map<String, Object> attributes) {

	public void addStringAttribute(String key, String value) {
		this.attributes.put(key, value);
	}

	public void addNumberAttribute(String key, Long value) {
		this.attributes.put(key, value);
	}
}