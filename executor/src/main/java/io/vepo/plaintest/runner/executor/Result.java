package io.vepo.plaintest.runner.executor;

import static java.lang.System.currentTimeMillis;
import static org.apache.commons.lang3.builder.ToStringStyle.SHORT_PREFIX_STYLE;

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
            start = end = currentTimeMillis();
            properties = new HashMap<>();
            results = new ArrayList<>();
            fails = new ArrayList<>();
        }

        private ResultBuilder(Result result) {
            name = result.name;
            start = result.start;
            end = result.end;
            success = result.success;
            properties = result.properties;
            results = result.results;
            fails = result.fails;
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
            properties.put(key, value);
            return this;
        }

        public ResultBuilder result(Result result) {
            results.add(result);
            return this;
        }

        public ResultBuilder fail(Fail fail) {
            fails.add(fail);
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

    private final String name;
    private final long start;
    private final long end;
    private final boolean success;
    private final Map<String, Object> properties;
    private final List<Result> results;
    private final List<Fail> fails;

    private Result(ResultBuilder builder) {
        name = builder.name;
        start = builder.start;
        end = builder.end;
        success = builder.success;
        properties = builder.properties;
        results = builder.results;
        fails = builder.fails;
    }

    public String getName() {
        return name;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }

    public boolean isSuccess() {
        return success;
    }

    public Map<String, Object> getProperties() {
        return properties;
    }

    public List<Result> getResults() {
        return results;
    }

    public List<Fail> getFails() {
        return fails;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String property, Class<T> requiredClass) {
        if (properties.containsKey(property)) {
            Object value = properties.get(property);
            if (!requiredClass.isInstance(value)) {
                if (Number.class.isAssignableFrom(requiredClass) && requiredClass == Long.class) {
                    return (T) ((Long) (((Number) value).longValue()));
                }
            } else {
                return (T) value;
            }
        }
        return null;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name).append(start).append(end).append(success).append(properties)
                .append(results).append(fails).hashCode();
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
        return new EqualsBuilder().append(name, other.name).append(start, other.start).append(end, other.end)
                .append(success, other.success).append(properties, other.properties).append(results, other.results)
                .append(fails, other.fails).isEquals();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, SHORT_PREFIX_STYLE).append("name", name).append("start", start)
                .append("end", end).append("success", success).append("properties", properties)
                .append("results", results).append("fails", fails).toString();
    }

}
