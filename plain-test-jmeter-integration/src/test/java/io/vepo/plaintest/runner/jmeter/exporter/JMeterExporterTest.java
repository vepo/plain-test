package io.vepo.plaintest.runner.jmeter.exporter;

import static io.vepo.plaintest.SuiteFactory.parseSuite;
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

	@Test
	public void emptySuiteTest() throws IOException, InterruptedException {
		JMeterExporter exporter = new JMeterExporter();
		InetSocketAddress clientAddress = getClient().remoteAddress();
		String jmxContents = exporter.export(parseSuite(HTTP_GET.replace("${host}", clientAddress.getHostName())
				.replace("${port}", Integer.toString(clientAddress.getPort()))));

		validateHttp("/request", "GET", 200, "OK", 1,
				() -> execute(jmxContents, event -> assertTrue(event.getResult().isSuccessful())));
		;
	}

}
