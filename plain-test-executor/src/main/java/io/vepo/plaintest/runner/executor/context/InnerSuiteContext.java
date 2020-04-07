package io.vepo.plaintest.runner.executor.context;

import java.nio.file.Path;

import io.vepo.plaintest.runner.executor.Result;

public class InnerSuiteContext implements Context {

	private Path workingDirectory;
	private Context parentContext;

	public InnerSuiteContext(Context parentContext, Path workingDirectory) {
		this.parentContext = parentContext;
		this.workingDirectory = workingDirectory;
	}

	@Override
	public void addResult(Result result) {
	}

	@Override
	public Path getWorkingDirectory() {
		return workingDirectory;
	}

	@Override
	public String toString() {
		return "InnerSuiteContext [parentContext=" + parentContext + ", workingDirectory=" + workingDirectory + "]";
	}
}
