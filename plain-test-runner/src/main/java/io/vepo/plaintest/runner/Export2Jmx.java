package io.vepo.plaintest.runner;

import java.io.File;
import java.util.concurrent.Callable;

import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.protocol.http.sampler.HTTPSampler;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.collections.HashTree;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "jmx-export", description = "Generate a JMeter Test Suite", mixinStandardHelpOptions = true)
public class Export2Jmx implements Callable<Integer> {
	@Option(names = { "-i", "--input" })
	private File input;

	@Override
	public Integer call() throws Exception {
		// JMeter Engine
		StandardJMeterEngine jmeter = new StandardJMeterEngine();

		// JMeter initialization (properties, log levels, locale, etc)
		JMeterUtils.loadJMeterProperties("jmeter.properties");
		JMeterUtils.initLogging();// you can comment this line out to see extra log messages of i.e. DEBUG level
		JMeterUtils.initLocale();

		// JMeter Test Plan, basic all u JOrphan HashTree
		HashTree testPlanTree = new HashTree();

		// HTTP Sampler
		HTTPSampler httpSampler = new HTTPSampler();
		httpSampler.setDomain("example.com");
		httpSampler.setPort(80);
		httpSampler.setPath("/");
		httpSampler.setMethod("GET");

		// Loop Controller
		LoopController loopController = new LoopController();
		loopController.setLoops(1);
		loopController.addTestElement(httpSampler);
		loopController.setFirst(true);
		loopController.initialize();

		// Thread Group
		org.apache.jmeter.threads.ThreadGroup threadGroup = new org.apache.jmeter.threads.ThreadGroup();
		threadGroup.setNumThreads(1);
		threadGroup.setRampUp(1);
		threadGroup.setSamplerController(loopController);

		// Test Plan
		TestPlan testPlan = new TestPlan("Create JMeter Script From Java Code");

		// Construct Test Plan from previously initialized elements
		testPlanTree.add("testPlan", testPlan);
		testPlanTree.add("loopController", loopController);
		testPlanTree.add("threadGroup", threadGroup);
		testPlanTree.add("httpSampler", httpSampler);

		// Run Test Plan
		jmeter.configure(testPlanTree);
		jmeter.run();
		return 0;
	}

}
