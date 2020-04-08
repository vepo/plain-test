package io.vepo.plaintest.runner.executor;

import java.util.List;

public class Result {

	private String name;
	private long start;
	private long end;
	private boolean success;
	private String stdout;
	private String stderr;
	private List<Result> results;
	private List<Fail> fails;

	public Result(String name, long start, long end, boolean success, String stdout, String stderr,
			List<Result> results, List<Fail> fails) {
		this.name = name;
		this.start = start;
		this.end = end;
		this.success = success;
		this.stdout = stdout;
		this.stderr = stderr;
		this.results = results;
		this.fails = fails;
	}

	public Result() {
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getStdout() {
		return stdout;
	}

	public void setStdout(String stdout) {
		this.stdout = stdout;
	}

	public String getStderr() {
		return stderr;
	}

	public void setStderr(String stderr) {
		this.stderr = stderr;
	}

	public List<Result> getResults() {
		return results;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	public List<Fail> getFails() {
		return fails;
	}

	public void setFails(List<Fail> fails) {
		this.fails = fails;
	}

	@SuppressWarnings("unchecked")
	public <T> T get(String property, Class<T> requiredClass) {
		switch (property) {
		case "stdout": {
			if (requiredClass == String.class) {
				return (T) this.stdout;
			} else {
				throwUnexpectedType(property, requiredClass, String.class);
			}
		}
		case "stderr": {
			if (requiredClass == String.class) {
				return (T) this.stderr;
			} else {
				throwUnexpectedType(property, requiredClass, String.class);
			}
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + property);
		}
	}

	private <T> void throwUnexpectedType(String property, Class<T> requiredType, Class<?> currentType) {
		throw new IllegalArgumentException(
				"Unexpected type: " + property + " has type " + currentType + " but was required " + requiredType);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (end ^ (end >>> 32));
		result = prime * result + ((fails == null) ? 0 : fails.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((results == null) ? 0 : results.hashCode());
		result = prime * result + (int) (start ^ (start >>> 32));
		result = prime * result + ((stderr == null) ? 0 : stderr.hashCode());
		result = prime * result + ((stdout == null) ? 0 : stdout.hashCode());
		result = prime * result + (success ? 1231 : 1237);
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
		Result other = (Result) obj;
		if (end != other.end)
			return false;
		if (fails == null) {
			if (other.fails != null)
				return false;
		} else if (!fails.equals(other.fails))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (results == null) {
			if (other.results != null)
				return false;
		} else if (!results.equals(other.results))
			return false;
		if (start != other.start)
			return false;
		if (stderr == null) {
			if (other.stderr != null)
				return false;
		} else if (!stderr.equals(other.stderr))
			return false;
		if (stdout == null) {
			if (other.stdout != null)
				return false;
		} else if (!stdout.equals(other.stdout))
			return false;
		if (success != other.success)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Result [name=" + name + ", start=" + start + ", end=" + end + ", success=" + success + ", stdout="
				+ stdout + ", stderr=" + stderr + ", results=" + results + ", fails=" + fails + "]";
	}

}
