package io.vepo.plaintest;

import static java.util.Objects.nonNull;
import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import java.util.Optional;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import io.vepo.plaintest.exceptions.PropertyNotDefinedException;

public abstract class SuiteChild {
	private final int index;
	private final Suite parent;

	protected SuiteChild(int index, Suite parent) {
		this.index = index;
		this.parent = parent;
	}

	public int getIndex() {
		return index;
	}

	public Suite getParent() {
		return parent;
	}

	public <T> T findRequiredPropertyValue(String key) {
		if (nonNull(parent)) {
			for (int currIndex = index - 1; currIndex >= 0; --currIndex) {
				SuiteChild curr = parent.getChild(currIndex);
				if (curr instanceof Properties && ((Properties) curr).hasValue(key)) {
					return ((Properties) curr).getValue(key);
				}
			}
			return parent.findRequiredPropertyValue(key);
		}
		throw new PropertyNotDefinedException("Could not find property: " + key);
	}

	public <T> Optional<T> findOptionalPropertyValue(String key) {
		if (nonNull(parent)) {
			for (int currIndex = index - 1; currIndex >= 0; --currIndex) {
				SuiteChild curr = parent.getChild(currIndex);
				if (curr instanceof Properties && ((Properties) curr).hasValue(key)) {
					return Optional.of(((Properties) curr).getValue(key));
				}
			}
			return parent.findOptionalPropertyValue(key);
		}
		return Optional.empty();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(index).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		requireNonNull(obj, "Null should be checked on parent class");
		if (getClass() != obj.getClass()) {
			return false;
		}
		SuiteChild other = (SuiteChild) obj;
		return new EqualsBuilder().append(index, other.index).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE).append("index", index).toString();
	}

}
