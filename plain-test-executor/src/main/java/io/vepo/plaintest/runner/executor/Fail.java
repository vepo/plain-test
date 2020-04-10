package io.vepo.plaintest.runner.executor;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class Fail {
	private FailReason reason;
	private String message;

	public Fail(FailReason reason, String message) {
		this.reason = reason;
		this.message = message;
	}

	public Fail() {
	}

	public FailReason getReason() {
		return this.reason;
	}

	public void setReason(FailReason reason) {
		this.reason = reason;
	}

	public String getMessage() {
		return this.message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.reason).append(this.message).hashCode();
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
		return new EqualsBuilder().append(this.reason, otherObject.reason).append(this.message, otherObject.message)
				.isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("reason", this.reason).append("message", this.message).toString();
	}

}
