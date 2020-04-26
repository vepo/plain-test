package io.vepo.plaintest;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public abstract class NamedSuiteChild extends SuiteChild {
	private final String name;

	protected NamedSuiteChild(int index, String name) {
		super(index);
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().appendSuper(super.hashCode()).append(name).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		NamedSuiteChild other = (NamedSuiteChild) obj;
		return new EqualsBuilder().appendSuper(super.equals(obj)).append(name, other.name).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE).appendSuper(super.toString()).append("name", name)
				.toString();
	}

}
