package io.vepo.plaintest.runner.jmeter.exporter;

import org.apache.jmeter.samplers.AbstractSampler;

import io.vepo.plaintest.Step;

public interface StepJMeterExporter {

	public String name();

	public AbstractSampler createSampler(Step step);

}
