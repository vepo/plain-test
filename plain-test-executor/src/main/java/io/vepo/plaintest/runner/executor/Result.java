package io.vepo.plaintest.runner.executor;

import java.util.List;

public record Result(String name, long start, long end, boolean success, String stdout, String stderr,
		List<Result> results, List<Fail> fails) {

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
}
