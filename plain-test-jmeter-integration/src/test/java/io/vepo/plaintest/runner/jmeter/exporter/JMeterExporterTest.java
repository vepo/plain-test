package io.vepo.plaintest.runner.jmeter.exporter;

import static io.vepo.plaintest.SuiteFactory.parseSuite;
import static java.util.regex.Pattern.quote;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;

import org.junit.jupiter.api.Test;

public class JMeterExporterTest extends AbstractJMeterExecutorTest {
	private static final String HTTP_GET = "Suite HttpGet {\n" + //
			"    HTTP GetRequest {\n" + //
			"        url: \"http://${host}:${port}/request\"\n" + //
			"        method: \"GET\"\n" + //
			"    }\n" + //
			"}";

	private static final String HTTP_INNER_GET = "Suite HttpGet {\n" + //
			"    HTTP GetRequest {\n" + //
			"        url: \"http://${host}:${port}/request\"\n" + //
			"        method: \"GET\"\n" + //
			"    }\n" + //
			"    Suite InnerGet {\n" + //
			"        HTTP GetRequest {\n" + //
			"            url: \"http://${host}:${port}/inner-request\"\n" + //
			"            method: \"GET\"\n" + //
			"        }\n" + //
			"    }" + //
			"}";

	private static final String HTTP_MULTI_LEVEL_GET = "Suite HttpGet1 {\n" + //
			"    HTTP GetRequest1 {\n" + //
			"        url: \"http://${host}:${port}/request\"\n" + //
			"        method: \"GET\"\n" + //
			"    }\n" + //
			"    Suite InnerGet2 {\n" + //
			"        HTTP GetRequest2 {\n" + //
			"            url: \"http://${host}:${port}/inner-request-2\"\n" + //
			"            method: \"GET\"\n" + //
			"        }\n" + //
			"        Suite InnerGet2 {\n" + //
			"            HTTP GetRequest2 {\n" + //
			"                url: \"http://${host}:${port}/inner-request-3\"\n" + //
			"                method: \"GET\"\n" + //
			"            }\n" + //
			"    }" + //
			"    }" + //
			"}";

	@Test
	public void singleSuiteTest() throws IOException, InterruptedException {
		JMeterExporter exporter = new JMeterExporter();
		InetSocketAddress clientAddress = getClient().remoteAddress();
		String jmxContents = exporter
				.export(parseSuite(HTTP_GET.replaceAll(quote("${host}"), clientAddress.getHostName())
						.replaceAll(quote("${port}"), Integer.toString(clientAddress.getPort()))));

		validateHttp("/request", "GET", 200, "OK", 1,
				() -> execute(jmxContents, event -> assertTrue(event.getResult().isSuccessful())));
		;
	}

	@Test
	public void innerSuiteTest() throws IOException, InterruptedException {
		JMeterExporter exporter = new JMeterExporter();
		InetSocketAddress clientAddress = getClient().remoteAddress();
		String jmxContents = exporter
				.export(parseSuite(HTTP_INNER_GET.replaceAll(quote("${host}"), clientAddress.getHostName())
						.replaceAll(quote("${port}"), Integer.toString(clientAddress.getPort()))));
		validateHttp("/inner-request", "GET", 200, "OK", 1, () -> validateHttp("/request", "GET", 200, "OK", 1,
				() -> execute(jmxContents, event -> assertTrue(event.getResult().isSuccessful()))));
	}

	@Test
	public void multiLevelSuiteTest() throws IOException, InterruptedException {
		JMeterExporter exporter = new JMeterExporter();
		InetSocketAddress clientAddress = getClient().remoteAddress();
		String jmxContents = exporter
				.export(parseSuite(HTTP_MULTI_LEVEL_GET.replaceAll(quote("${host}"), clientAddress.getHostName())
						.replaceAll(quote("${port}"), Integer.toString(clientAddress.getPort()))));
		validateHttp("/inner-request-3", "GET", 200, "OK", 1,
				() -> validateHttp("/inner-request-2", "GET", 200, "OK", 1, () -> validateHttp("/request", "GET", 200,
						"OK", 1, () -> execute(jmxContents, event -> assertTrue(event.getResult().isSuccessful())))));
	}

}
