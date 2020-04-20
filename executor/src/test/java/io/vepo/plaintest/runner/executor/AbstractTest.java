package io.vepo.plaintest.runner.executor;

import static java.util.Objects.isNull;

import java.util.Objects;
import java.util.Optional;

public abstract class AbstractTest {

	protected Optional<Result> find(Result result, String name) {
		if (isNull(result)) {
			return Optional.empty();
		} else if (result.getName().equals(name)) {
			return Optional.of(result);
		} else {
			return result.getResults().stream().map(r -> find((Result) r, name).orElse(null)).filter(Objects::nonNull)
					.findFirst();
		}
	}
}
