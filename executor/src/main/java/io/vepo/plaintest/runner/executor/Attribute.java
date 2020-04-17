package io.vepo.plaintest.runner.executor;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Attribute<T> {
	public final String key;
	public final Class<?> type;
	private final boolean required;

	public static <A> Attribute<A> createAttribute(String key, Class<?> type, boolean required) {
		return new Attribute<>(key, type, required);
	}

	private Attribute(String key, Class<?> type, boolean required) {
		this.key = key;
		this.type = type;
		this.required = required;
	}

	public String getKey() {
		return this.key;
	}

	public Class<?> getType() {
		return this.type;
	}

	public boolean isRequired() {
		return required;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.key).append(this.type).append(this.required).hashCode();
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
		final Attribute<?> other = (Attribute<?>) obj;
		return new EqualsBuilder().append(this.key, other.key).append(this.type, other.type)
				.append(this.required, other.required).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE).append("key", this.key).append("type", this.type)
				.append("required", this.required).toString();
	}

}
