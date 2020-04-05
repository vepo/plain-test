package io.vepo.plaintest.runner;

import java.io.File;
import java.util.concurrent.Callable;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "jmx-export", description = "Generate a JMeter Test Suite", mixinStandardHelpOptions = true)
public class Export2Jmx implements Callable<Integer> {
	@Option(names = { "-i", "--input" })
	private File input;

	public Integer call() throws Exception {
		System.out.println(input.getAbsolutePath());
		return 0;
	}

}
