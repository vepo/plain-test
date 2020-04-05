package io.vepo.plaintest.runner.executor.context;

import java.io.File;
import java.nio.file.Paths;

import io.vepo.plaintest.runner.executor.Result;

public class InnerSuiteContext implements Context {
	public InnerSuiteContext(Context parentContext) {
	}

	@Override
	public void addResult(Result result) {
	}

	@Override
	public File getWorkingDirectory() {
		return Paths.get(".").toFile();
	}
}
