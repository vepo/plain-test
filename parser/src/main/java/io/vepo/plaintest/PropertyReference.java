package io.vepo.plaintest;

import static java.util.regex.Pattern.compile;
import static java.util.regex.Pattern.quote;

import java.util.regex.Pattern;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class PropertyReference {
	static Pattern regex = compile(quote("${") + "([A-Za-z][._\\-A-Za-z0-9]*)" + quote("}"));

	public static boolean matches(String value) {
		return regex.matcher(value).matches();
	}

	private final String name;

	public PropertyReference(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(name).hashCode();
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
		PropertyReference other = (PropertyReference) obj;
		return new EqualsBuilder().append(name, other.name).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("name", name).toString();
	}
}
