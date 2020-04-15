package io.vepo.plaintest.runner;

import java.io.File;
import java.util.concurrent.Callable;

import io.vepo.plaintest.runner.jmeter.exporter.JMeterExporter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "jmx-export", description = "Generate a JMeter Test Suite", mixinStandardHelpOptions = true)
public class Export2Jmx implements Callable<Integer> {
	@Option(names = { "-i", "--input" })
	private File input;

	@Override
	public Integer call() throws Exception {
		JMeterExporter exporter = new JMeterExporter();
		exporter.export(null);
		return 0;
	}

}
