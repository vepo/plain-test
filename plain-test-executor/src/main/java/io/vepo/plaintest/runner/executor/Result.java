package io.vepo.plaintest.runner.executor;

import java.util.List;

public record Result(String name, long start, long end, boolean success, String stdout, String stderr,
		List<Result> results, List<Fail> fails) {
}
