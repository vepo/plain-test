package io.vepo.plaintest.runner.executor;

import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Fail {
	private final FailReason reason;
	private final String message;

	public Fail(FailReason reason, String message) {
		this.reason = reason;
		this.message = message;
	}

	public FailReason getReason() {
		return reason;
	}

	public String getMessage() {
		return message;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(reason).append(message).hashCode();
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
		final Fail otherObject = (Fail) obj;
		return new EqualsBuilder().append(reason, otherObject.reason).append(message, otherObject.message).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, SHORT_PREFIX_STYLE).append("reason", reason).append("message", message)
				.toString();
	}

}
