package io.vepo.plaintest.runner.executor.context;

import java.nio.file.Path;

import io.vepo.plaintest.runner.executor.Result;

public interface Context {

	public void addResult(Result result);
	
	public Path getWorkingDirectory();
}
