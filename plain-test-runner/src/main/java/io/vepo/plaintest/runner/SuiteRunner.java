package io.vepo.plaintest.runner;

import static io.vepo.plaintest.SuiteFactory.parseSuite;
import static java.nio.file.Files.readAllBytes;

import java.io.File;
import java.util.concurrent.Callable;

import io.vepo.plaintest.runner.executor.PlainTestExecutor;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Parameters;
import picocli.CommandLine.Spec;

@Command(name = "runner", version = "1.0", subcommands = Export2Jmx.class, mixinStandardHelpOptions = true)
public class SuiteRunner implements Callable<Integer> {
	@Spec
	CommandSpec spec;

	@Parameters(index = "0..*")
	File[] suites;

	public static void main(String... args) {
		int exitCode = new CommandLine(new SuiteRunner()).execute(args);
		System.exit(exitCode);
	}

	@Override
	public Integer call() throws Exception {
		if (suites == null || suites.length == 0) {
			spec.commandLine().usage(System.out);
			return 1;
		} else {
			PlainTestExecutor executor = new PlainTestExecutor();
			for (File suite : suites) {
				executor.execute(parseSuite(new String(readAllBytes(suite.toPath()))));
			}
			return 0;
		}

	}
}
