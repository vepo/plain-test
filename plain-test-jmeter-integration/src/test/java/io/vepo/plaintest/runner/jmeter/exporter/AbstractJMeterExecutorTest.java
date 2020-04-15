package io.vepo.plaintest.runner.jmeter.exporter;

import static java.nio.file.Files.createTempFile;
import static org.apache.jmeter.save.SaveService.loadTree;
import static org.apache.jmeter.util.JMeterUtils.getPropDefault;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.atLeast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.apache.commons.io.IOUtils;
import org.apache.jmeter.engine.StandardJMeterEngine;
import org.apache.jmeter.reporters.ResultCollector;
import org.apache.jmeter.reporters.Summariser;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jorphan.collections.HashTree;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.client.MockServerClient;
import org.mockserver.junit.jupiter.MockServerExtension;
import org.mockserver.model.HttpRequest;

@ExtendWith(MockServerExtension.class)
public abstract class AbstractJMeterExecutorTest {

	private class TestResultCollector extends ResultCollector {

		/**
		 *
		 */
		private static final long serialVersionUID = 1915750716772481390L;
		private Consumer<SampleEvent> callback;

		public TestResultCollector(Summariser summer, Consumer<SampleEvent> callback) {
			super(summer);
			this.callback = callback;
		}

		@Override
		public void sampleOccurred(SampleEvent event) {
			super.sampleOccurred(event);
			callback.accept(event);
		}

	}

	private MockServerClient client;

	public MockServerClient getClient() {
		return client;
	}

	@BeforeEach
	public void setup(MockServerClient client) {
		this.client = client;
	}

	@AfterEach
	public void tearDown() {
		client.reset();
	}

	protected void execute(String jmxContents, Consumer<SampleEvent> callback) {
		try {
			StandardJMeterEngine jmeter = new StandardJMeterEngine();
			Path file = createTempFile("Test", ".jmx");
			try (FileOutputStream fos = new FileOutputStream(file.toFile())) {
				IOUtils.write(jmxContents.getBytes(), fos);
			}

			HashTree testTree = loadTree(file.toFile());

			Summariser summer = null;
			String summariserName = getPropDefault("summariser.name", "summary");
			if (summariserName.length() > 0) {
				summer = new Summariser(summariserName);
			}

			testTree.add(testTree.getArray()[0], new TestResultCollector(summer, callback));

			jmeter.configure(testTree);
			jmeter.run();
		} catch (IOException e) {
			fail(e);
		}
	}

	protected void validateHttp(String path, String method, int statusCode, String responseBody, int times,
			Runnable code) {
		HttpRequest serverRequest = request().withMethod(method).withPath(path);

		client.when(serverRequest).respond(response().withStatusCode(statusCode).withBody(responseBody));

		code.run();

		client.verify(serverRequest, atLeast(times));
	}
}
