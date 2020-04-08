package io.vepo.plaintest.runner.executor;

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
		return reason;
	}

	public void setReason(FailReason reason) {
		this.reason = reason;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((reason == null) ? 0 : reason.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Fail other = (Fail) obj;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (reason != other.reason)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Fail [reason=" + reason + ", message=" + message + "]";
	}

}
