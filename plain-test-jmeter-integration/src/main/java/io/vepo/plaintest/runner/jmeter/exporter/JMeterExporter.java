package io.vepo.plaintest.runner.jmeter.exporter;

import static java.nio.charset.Charset.defaultCharset;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.isNull;
import static org.apache.commons.text.StringEscapeUtils.escapeJava;
import static org.apache.jmeter.save.SaveService.loadProperties;
import static org.apache.jmeter.save.SaveService.saveTree;
import static org.apache.jmeter.util.JMeterUtils.loadJMeterProperties;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.plaintest.Suite;

public class JMeterExporter {
	private static final Logger logger = LoggerFactory.getLogger(JMeterExporter.class);
	private Map<String, StepJMeterExporter> stepExporters;

	public JMeterExporter() {
		stepExporters = new HashMap<>();
		ServiceLoader.load(StepJMeterExporter.class).forEach(exporter -> stepExporters.put(exporter.name(), exporter));
	}

	public String export(Suite rootSuite) {
		logger.debug("Exporting suite: {}", rootSuite);
		setupJMeter();
		// JMeter Test Plan, basic all u JOrphan HashTree
		HashTree testPlanTree = new ListedHashTree();

		// Test Plan
		TestPlan testPlan = new TestPlan(rootSuite.getName());
		testPlan.setComment("");
		testPlan.setFunctionalMode(false);
		testPlan.setTearDownOnShutdown(true);
		testPlan.setSerialized(false);
		testPlan.setUserDefinedVariables(new Arguments());
		testPlan.setTestPlanClasspath("");
		setupGuiValues(testPlan);

		org.apache.jmeter.threads.ThreadGroup threadGroup = new org.apache.jmeter.threads.ThreadGroup();
		threadGroup.setName("Thread");
		threadGroup.setNumThreads(1);
		threadGroup.setRampUp(1);
		setupGuiValues(threadGroup);

		LoopController loopController = new LoopController();
		loopController.setLoops(1);
		loopController.setFirst(true);
		loopController.initialize();
		setupGuiValues(loopController);
		threadGroup.setSamplerController(loopController);

		testPlanTree.add(testPlan);

		HashTree threadGroupHashTree = testPlanTree.add(testPlan, threadGroup);

		rootSuite.forEachOrdered(suite -> logger.info("Adding suite: {}", suite), step -> {
			logger.info("Adding Step: {}", step);
			threadGroupHashTree.add(stepExporters.get(step.getPlugin()).createSampler(step));
		});

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			loadProperties();
			saveTree(testPlanTree, baos);
			return new String(baos.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void setupJMeter() {
		// JMeter initialization (properties, log levels, locale, etc)
		try {
			Path jmeterProperties = createTempFile("jmeter", ".properties");
			Path upgradeProperties = createTempFile("upgrade", ".properties");
			Path saveServiceProperties = createTempFile("saveService", ".properties");
			InputStream jmeterPropertiesStream = JMeterExporter.class.getResourceAsStream("/jmeter.properties");
			if (isNull(jmeterPropertiesStream)) {
				throw new IllegalStateException("Could not load jmeter.properties");
			}

			InputStream upgradePropertiesStream = JMeterExporter.class.getResourceAsStream("/upgrade.properties");
			if (isNull(upgradePropertiesStream)) {
				throw new IllegalStateException("Could not load upgrade.properties");
			}

			InputStream saveServicePropertiesStream = JMeterExporter.class
					.getResourceAsStream("/saveservice.properties");
			if (isNull(saveServicePropertiesStream)) {
				throw new IllegalStateException("Could not load saveservice.properties");
			}

			String jmeterPropertiesContents = IOUtils.toString(jmeterPropertiesStream, defaultCharset())
					.replace("${upgrade.properties}", escapeJava(upgradeProperties.toString()))
					.replace("${saveservice.properties}", escapeJava(saveServiceProperties.toString()));
			try (InputStream inputStream = new ByteArrayInputStream(jmeterPropertiesContents.getBytes())) {
				Files.copy(inputStream, jmeterProperties, REPLACE_EXISTING);
			}
			Files.copy(upgradePropertiesStream, upgradeProperties, REPLACE_EXISTING);
			Files.copy(saveServiceProperties, upgradeProperties, REPLACE_EXISTING);

			loadJMeterProperties(jmeterProperties.toFile().getAbsolutePath());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void setupGuiValues(TestElement element) {
		element.setProperty(TestElement.GUI_CLASS, element.getClass().getSimpleName() + "Gui");
		element.setProperty(TestElement.TEST_CLASS, element.getClass().getSimpleName());

	}

}
