package io.vepo.plaintest.runner.executor;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Attribute<T> {
	public final String key;
	public final Class<?> type;

	public Attribute(String key, Class<?> type) {
		this.key = key;
		this.type = type;
	}

	public String key() {
		return this.key;
	}

	public Class<?> type() {
		return this.type;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.key).append(this.type).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		final Attribute<?> otherObject = (Attribute<?>) obj;
		return new EqualsBuilder().append(this.key, otherObject.key).append(this.type, otherObject.type).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("key", this.key).append("type", this.type).toString();
	}

}
