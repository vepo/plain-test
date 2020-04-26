package io.vepo.plaintest.runner;

import static io.vepo.plaintest.SuiteFactory.parseSuite;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

import java.io.File;
import java.util.concurrent.Callable;

import io.vepo.plaintest.runner.jmeter.exporter.JMeterExporter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "jmx-export", description = "Generate a JMeter Test Suite", mixinStandardHelpOptions = true)
public class Export2Jmx implements Callable<Integer> {
	@Option(names = { "-i", "--input" }, required = true)
	private File input;

	@Option(names = { "-o", "--output" }, required = true)
	private File output;

	@Override
	public Integer call() throws Exception {
		JMeterExporter exporter = new JMeterExporter();

		String contents = exporter.export(parseSuite(new String(readAllBytes(input.toPath()))));
		write(output.toPath(), contents.getBytes(), TRUNCATE_EXISTING);
		return 0;
	}

}
