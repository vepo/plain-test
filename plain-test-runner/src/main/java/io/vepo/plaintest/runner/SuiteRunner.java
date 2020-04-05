package io.vepo.plaintest.runner;

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Spec;

@Command(name = "runner", version = "1.0", subcommands = Export2Jmx.class, mixinStandardHelpOptions = true)
public class SuiteRunner implements Callable<Integer> {
	@Spec
	CommandSpec spec;

	public static void main(String... args) {
		int exitCode = new CommandLine(new SuiteRunner()).execute(args);
		System.exit(exitCode);
	}

	public Integer call() throws Exception {
		spec.commandLine().usage(System.out);
		return 0;
	}
}
