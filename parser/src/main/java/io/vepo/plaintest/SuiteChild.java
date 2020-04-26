package io.vepo.plaintest;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class SuiteChild {
	private final int index;

	protected SuiteChild(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(index).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
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
