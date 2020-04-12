package io.vepo.plaintest;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class Assertion<T> {
	private final String property;
	private final String verb;
	private final T value;

	public Assertion(String property, String verb, T value) {
		this.property = property;
		this.verb = verb;
		this.value = value;
	}

	public String getProperty() {
		return property;
	}

	public String getVerb() {
		return verb;
	}

	public T getValue() {
		return value;
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