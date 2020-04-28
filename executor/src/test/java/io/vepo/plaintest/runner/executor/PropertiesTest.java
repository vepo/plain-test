package io.vepo.plaintest.runner.executor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;
import java.util.Properties;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.vepo.plaintest.Suite;
import io.vepo.plaintest.SuiteFactory;

@DisplayName("Properties")
public class PropertiesTest extends AbstractHttpTest {

	private static final String HTTP_GET_TEST_SUITE = "Suite HttpGet {\n" + //
			"    Properties {\n" + //
			"        url: \"http://${host}:${port}/defaultGet\"\n" + //
			"        method: \"GET\"\n" + //
			"    }\n" + //
			"    HTTP GetRequest {\n" + //
			"        url: ${url}\n" + //
			"        method: \"${method}\"\n" + //
			"    }\n" + //
			"}";

	@Test
	@DisplayName("It should accept Properties on Execution")
	public void executeWithPropertiesTest() {
		InetSocketAddress remoteAddress = remoteAddress();
		validateHttp("/defaultGet", "GET", 200, "{\"response\":\"OK\"}", 1, () -> {

			Suite suite = SuiteFactory.parseSuite(HTTP_GET_TEST_SUITE.replace("${host}", remoteAddress.getHostName())
					.replace("${port}", Integer.toString(remoteAddress.getPort())));
			PlainTestExecutor executor = new PlainTestExecutor();

			assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.isSuccess()))
					.satisfies(result -> assertThat(find(result, "HttpGet")).isPresent().get()
							.satisfies(r -> assertTrue(r.isSuccess())))
					.satisfies(result -> assertThat(find(result, "GetRequest")).isPresent().get()
							.satisfies(r -> assertTrue(r.isSuccess())));
		});
	}

	private static final String HTTP_GET_TEST_SUITE_NO_PROPERTIES = "Suite HttpGet {\n" + //
			"    HTTP GetRequest {\n" + //
			"        url: ${url}\n" + //
			"        method: \"${method}\"\n" + //
			"    }\n" + //
			"}";

	@Test
	@DisplayName("It should accept Properties Load on Execution")
	public void executeWithNoPropertiesTest() {
		InetSocketAddress remoteAddress = remoteAddress();
		validateHttp("/defaultGet", "GET", 200, "{\"response\":\"OK\"}", 1, () -> {

			Suite suite = SuiteFactory
					.parseSuite(HTTP_GET_TEST_SUITE_NO_PROPERTIES.replace("${host}", remoteAddress.getHostName())
							.replace("${port}", Integer.toString(remoteAddress.getPort())));
			PlainTestExecutor executor = new PlainTestExecutor();

			Properties properties = new Properties();
			properties.put("url", "http://${host}:${port}/defaultGet");
			properties.put("method", "GET");
			executor.load(properties);

			assertThat(executor.execute(suite)).satisfies(result -> assertTrue(result.isSuccess()))
					.satisfies(result -> assertThat(find(result, "HttpGet")).isPresent().get()
							.satisfies(r -> assertTrue(r.isSuccess())))
					.satisfies(result -> assertThat(find(result, "GetRequest")).isPresent().get()
							.satisfies(r -> assertTrue(r.isSuccess())));
		});
	}
}
