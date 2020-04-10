package io.vepo.plaintest.runner.executor;

import static java.lang.System.currentTimeMillis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Result {

	public static class ResultBuilder {
		private String name;
		private long start;
		private long end;
		private boolean success;
		private Map<String, Object> properties;
		private List<Result> results;
		private List<Fail> fails;

		private ResultBuilder() {
			this.start = this.end = currentTimeMillis();
			this.properties = new HashMap<>();
			this.results = new ArrayList<>();
			this.fails = new ArrayList<>();
		}

		private ResultBuilder(Result result) {
			this.name = result.name;
			this.start = result.start;
			this.end = result.end;
			this.success = result.success;
			this.properties = result.properties;
			this.results = result.results;
			this.fails = result.fails;
		}

		public ResultBuilder name(String name) {
			this.name = name;
			return this;
		}

		public ResultBuilder start(long start) {
			this.start = start;
			return this;
		}

		public ResultBuilder end(long end) {
			this.end = end;
			return this;
		}

		public ResultBuilder success(boolean success) {
			this.success = success;
			return this;
		}

		public ResultBuilder property(String key, Object value) {
			this.properties.put(key, value);
			return this;
		}

		public ResultBuilder results(List<Result> results) {
			this.results.addAll(results);
			return this;
		}

		public ResultBuilder result(Result result) {
			this.results.add(result);
			return this;
		}

		public ResultBuilder fails(List<Fail> fails) {
			this.fails.addAll(fails);
			return this;
		}

		public ResultBuilder fail(Fail fail) {
			this.fails.add(fail);
			return this;
		}

		public Result build() {
			return new Result(this);
		}
	}

	public static ResultBuilder builder() {
		return new ResultBuilder();
	}

	public static ResultBuilder builder(Result result) {
		return new ResultBuilder(result);
	}

	private String name;
	private long start;
	private long end;
	private boolean success;
	private Map<String, Object> properties;
	private List<Result> results;
	private List<Fail> fails;

	private Result(ResultBuilder builder) {
		this.name = builder.name;
		this.start = builder.start;
		this.end = builder.end;
		this.success = builder.success;
		this.properties = builder.properties;
		this.results = builder.results;
		this.fails = builder.fails;
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

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
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
		if (this.properties.containsKey(property)) {
			Object value = this.properties.get(property);
			if (!requiredClass.isInstance(value)) {
				throw new IllegalArgumentException("Unexpected type: " + property + " has type " + value.getClass()
						+ " but was required " + requiredClass);
			} else {
				return (T) value;
			}
		}
		return null;
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
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
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
		if (properties == null) {
			if (other.properties != null)
				return false;
		} else if (!properties.equals(other.properties))
			return false;
		if (success != other.success)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Result [name=" + name + ", start=" + start + ", end=" + end + ", success=" + success + ", properties="
				+ properties + ", results=" + results + ", fails=" + fails + "]";
	}

}
