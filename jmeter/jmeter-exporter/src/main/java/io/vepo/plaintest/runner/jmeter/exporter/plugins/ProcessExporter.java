package io.vepo.plaintest.runner.jmeter.exporter.plugins;

import org.apache.jmeter.protocol.system.SystemSampler;
import org.apache.jmeter.protocol.system.gui.SystemSamplerGui;
import org.apache.jmeter.samplers.AbstractSampler;
import org.apache.jmeter.testelement.TestElement;

import io.vepo.plaintest.Step;
import io.vepo.plaintest.runner.jmeter.exporter.StepExporter;

public class ProcessExporter implements StepExporter {

	@Override
	public String pluginName() {
		return "Process";
	}

	@Override
	public AbstractSampler createSampler(Step step) {
		SystemSampler systemSampler = new SystemSampler();
		systemSampler.setCommand(step.requiredAttribute("cmd"));
		systemSampler.setProperty(TestElement.TEST_CLASS, SystemSampler.class.getName());
		systemSampler.setProperty(TestElement.GUI_CLASS, SystemSamplerGui.class.getName());
		return systemSampler;
	}

}
