package io.vepo.plaintest.runner.executor.context;

import java.io.File;

import io.vepo.plaintest.runner.executor.Result;

public interface Context {

	public void addResult(Result result);
	
	public File getWorkingDirectory();
}
