package io.vepo.plaintest.runner.jmeter.exporter;

import static java.nio.charset.Charset.defaultCharset;
import static java.nio.file.Files.copy;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static java.util.Objects.isNull;
import static org.apache.commons.text.StringEscapeUtils.escapeJava;
import static org.apache.jmeter.save.SaveService.loadProperties;
import static org.apache.jmeter.save.SaveService.saveTree;
import static org.apache.jmeter.testelement.TestElement.GUI_CLASS;
import static org.apache.jmeter.testelement.TestElement.TEST_CLASS;
import static org.apache.jmeter.util.JMeterUtils.loadJMeterProperties;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.config.Arguments;
import org.apache.jmeter.config.gui.ArgumentsPanel;
import org.apache.jmeter.control.GenericController;
import org.apache.jmeter.control.LoopController;
import org.apache.jmeter.control.gui.LogicControllerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.testelement.TestPlan;
import org.apache.jorphan.collections.HashTree;
import org.apache.jorphan.collections.ListedHashTree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vepo.plaintest.Properties;
import io.vepo.plaintest.Step;
import io.vepo.plaintest.Suite;

public class JMeterExporter {
	private static final String EXT_PROPERTIES = ".properties";
	private static final Logger logger = LoggerFactory.getLogger(JMeterExporter.class);
	private Map<String, StepExporter> stepExporters;

	public JMeterExporter() {
		stepExporters = new HashMap<>();
		ServiceLoader.load(StepExporter.class).forEach(exporter -> stepExporters.put(exporter.pluginName(), exporter));
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

		rootSuite.forEachOrdered(suite -> createSuite(threadGroupHashTree, suite),
				step -> createStep(threadGroupHashTree, step),
				properties -> createProperties(threadGroupHashTree, properties));

		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			loadProperties();
			saveTree(testPlanTree, baos);
			return new String(baos.toByteArray());
		} catch (IOException e) {
			throw new IllegalStateException("Cannot write JMX!", e);
		}
	}

	private void createProperties(HashTree tree, Properties properties) {
		Arguments arguments = new Arguments();
		properties.getValues().forEach((key, value) -> arguments.addArgument(key, value.toString()));
		arguments.setProperty(TEST_CLASS, Arguments.class.getSimpleName());
		arguments.setProperty(GUI_CLASS, ArgumentsPanel.class.getSimpleName());
		tree.add(arguments);
	}

	private void createStep(HashTree tree, Step step) {
		logger.info("Adding Step: {}", step);
		tree.add(stepExporters.get(step.getPlugin()).createSampler(step));
	}

	private void createSuite(HashTree tree, Suite suite) {
		logger.info("Adding suite: {}", suite);
		GenericController controller = new GenericController();
		controller.setName(suite.getName());
		setupGuiValues(controller);
		HashTree innerTree = tree.add(controller);
		suite.forEachOrdered(innerSuite -> createSuite(innerTree, innerSuite),
				innerStep -> createStep(innerTree, innerStep));
	}

	private void setupJMeter() {
		// JMeter initialization (properties, log levels, locale, etc)
		try {
			Path jmeterProperties = createTempFile("jmeter", EXT_PROPERTIES);
			Path upgradeProperties = createTempFile("upgrade", EXT_PROPERTIES);
			Path saveServiceProperties = createTempFile("saveService", EXT_PROPERTIES);
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
				copy(inputStream, jmeterProperties, REPLACE_EXISTING);
			}
			copy(upgradePropertiesStream, upgradeProperties, REPLACE_EXISTING);
			copy(saveServicePropertiesStream, saveServiceProperties, REPLACE_EXISTING);

			loadJMeterProperties(jmeterProperties.toFile().getAbsolutePath());
		} catch (IOException e) {
			throw new IllegalStateException("Cannot read/write properties files!", e);
		}
	}

	public static void setupGuiValues(TestElement element) {
		if (element.getClass() == GenericController.class) {
			element.setProperty(GUI_CLASS, LogicControllerGui.class.getSimpleName());
		} else {
			element.setProperty(GUI_CLASS, element.getClass().getSimpleName() + "Gui");
		}
		element.setProperty(TEST_CLASS, element.getClass().getSimpleName());
	}

}
