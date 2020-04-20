package io.vepo.plaintest.runner.jmeter.exporter;

import org.apache.jmeter.samplers.AbstractSampler;

import io.vepo.plaintest.Step;

public interface StepExporter {

	public String pluginName();

	public AbstractSampler createSampler(Step step);

}
