package io.vepo.plaintest;

import java.util.Map;

public record Step(String plugin, String name, Map<String, Object> attributes, Map<String, Object> asserts) {

	public void addStringAttribute(String key, String value) {
		this.attributes.put(key, value);
	}

	public void addNumberAttribute(String key, Long value) {
		this.attributes.put(key, value);
	}
	
	public void addStringAssertionAttribute(String key, String value) {
		this.asserts.put(key, value);
	}

	public void addNumberAssertionAttribute(String key, Long value) {
		this.asserts.put(key, value);
	}
}