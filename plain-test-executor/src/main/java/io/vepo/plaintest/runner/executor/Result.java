package io.vepo.plaintest.runner.executor;

import static java.lang.System.currentTimeMillis;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

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
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getStart() {
		return this.start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return this.end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	public boolean isSuccess() {
		return this.success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public Map<String, Object> getProperties() {
		return this.properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	public List<Result> getResults() {
		return this.results;
	}

	public void setResults(List<Result> results) {
		this.results = results;
	}

	public List<Fail> getFails() {
		return this.fails;
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
		return new HashCodeBuilder().append(this.name).append(this.start).append(this.end).append(this.success)
				.append(this.properties).append(this.results).append(this.fails).hashCode();
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
		Result other = (Result) obj;
		return new EqualsBuilder().append(this.name, other.name).append(this.start, other.start)
				.append(this.end, other.end).append(this.success, other.success)
				.append(this.properties, other.properties).append(this.results, other.results)
				.append(this.fails, other.fails).isEquals();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("name", this.name).append("start", this.start).append("end", this.end)
				.append("success", this.success).append("properties", this.properties).append("results", this.results)
				.append("fails", this.fails).toString();
	}

}
