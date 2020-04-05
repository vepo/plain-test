package io.vepo.plaintest.runner.executor.context;

import java.io.File;
import java.nio.file.Paths;

import io.vepo.plaintest.runner.executor.Result;

public class RootSuiteContext implements Context {

	@Override
	public void addResult(Result result) {
	}

	@Override
	public File getWorkingDirectory() {
		return Paths.get(".").toFile();
	}

}
