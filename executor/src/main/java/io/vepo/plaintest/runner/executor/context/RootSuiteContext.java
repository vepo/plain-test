package io.vepo.plaintest.runner.executor.context;

import java.nio.file.Path;

import io.vepo.plaintest.runner.executor.Result;

public class RootSuiteContext implements Context {

	private Path workingDirectory;

	public RootSuiteContext(Path workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public RootSuiteContext() {
	}

	@Override
	public void addResult(Result result) {
		// not implemented yet...
	}

	@Override
	public Path getWorkingDirectory() {
		return workingDirectory;
	}

	@Override
	public String toString() {
		return "RootSuiteContext [workingDirectory=" + workingDirectory + "]";
	}
}
