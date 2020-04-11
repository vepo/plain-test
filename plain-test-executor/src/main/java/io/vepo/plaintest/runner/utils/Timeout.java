package io.vepo.plaintest.runner.utils;

import static java.lang.Thread.currentThread;

import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Timeout {
	private static final long MAX_ALLOWED_TIME = 250;
	private static final Logger logger = LoggerFactory.getLogger(Timeout.class);

	private Timeout() {
	}

	public static <T> Optional<T> executeWithTimeout(Callable<T> code, Optional<Long> timeout) {
		Future<T> execution = Executors.newSingleThreadExecutor().submit(code);
		T value;
		try {
			if (timeout.isPresent()) {
				value = execution.get(timeout.get() + MAX_ALLOWED_TIME, TimeUnit.MILLISECONDS);
			} else {
				value = execution.get();
			}
		} catch (InterruptedException e) {
			currentThread().interrupt();
			return null;
		} catch (ExecutionException e) {
			logger.warn("Execution error!", e);
			throw new IllegalStateException("Error test execution!", e);
		} catch (TimeoutException e) {
			return Optional.empty();
		}
		return Optional.of(value);
	}
}
