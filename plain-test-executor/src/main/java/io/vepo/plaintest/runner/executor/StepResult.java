package io.vepo.plaintest.runner.executor;

public class StepResult extends Result {
	private String name;
	private ExecutionStatus executionStatus;
	private String errorMessage;

	public StepResult(String name, ExecutionStatus executionStatus, String errorMessage) {
		this.name = name;
		this.executionStatus = executionStatus;
		this.errorMessage = errorMessage;
	}

	public StepResult() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ExecutionStatus getExecutionStatus() {
		return executionStatus;
	}

	public void setExecutionStatus(ExecutionStatus executionStatus) {
		this.executionStatus = executionStatus;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((executionStatus == null) ? 0 : executionStatus.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((errorMessage == null) ? 0 : errorMessage.hashCode());
		return result;
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
		StepResult other = (StepResult) obj;
		if (executionStatus != other.executionStatus) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}

		if (errorMessage == null) {
			if (other.errorMessage != null) {
				return false;
			}
		} else if (!errorMessage.equals(other.errorMessage)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return "StepResult [name=" + name + ", executionStatus=" + executionStatus + ", errorMessage=" + errorMessage
				+ "]";
	}

}
