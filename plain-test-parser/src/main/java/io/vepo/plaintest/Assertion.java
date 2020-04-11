package io.vepo.plaintest;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Assertion<T> {
	private String property;
	private String verb;
	private T value;

	public Assertion(String property, String verb, T value) {
		this.property = property;
		this.verb = verb;
		this.value = value;
	}

	public Assertion() {
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getVerb() {
		return verb;
	}

	public void setVerb(String verb) {
		this.verb = verb;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.property).append(this.verb).append(this.value).hashCode();
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
		Assertion<?> other = (Assertion<?>) obj;
		return new EqualsBuilder().append(this.property, other.property).append(this.verb, other.verb)
				.append(this.value, other.value).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("property", this.property)
				.append("verb", this.verb).append("value", this.value).toString();
	}

}