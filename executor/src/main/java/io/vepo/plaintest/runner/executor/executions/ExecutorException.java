package io.vepo.plaintest.runner.executor.executions;

import java.util.concurrent.ExecutionException;

public class ExecutorException extends RuntimeException {

	public ExecutorException(String message, ExecutionException cause) {
		super(message, cause);
	}

	private static final long serialVersionUID = -1592383656498053644L;

}
